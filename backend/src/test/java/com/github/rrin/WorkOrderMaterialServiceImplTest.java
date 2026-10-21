package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.inventory.Location;
import com.github.rrin.inventory.LocationKind;
import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.dto.MaterialMovementRequest;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.repository.StockMovementRepository;
import com.github.rrin.inventory.service.LocationService;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.inventory.service.StockReservationService;
import com.github.rrin.inventory.service.implementation.WorkOrderMaterialServiceImpl;
import com.github.rrin.item.Item;
import com.github.rrin.production.*;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderMaterialServiceImplTest {

    @Mock
    private WorkOrderComponentRepository componentRepository;

    @Mock
    private WorkOrderStepRepository stepRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private StockMovementService movementService;

    @Mock
    private StockReservationService reservationService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private WorkOrderMaterialServiceImpl service;

    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final Employee worker = Employee.builder().id(UUID.randomUUID()).name("worker").build();
    private final WorkOrder workOrder = WorkOrder.builder().id(UUID.randomUUID()).code("WO-1").status(WorkOrderStatus.IN_PROGRESS).build();
    private final WorkOrderStep step = WorkOrderStep.builder().id(UUID.randomUUID()).workOrder(workOrder)
            .status(WorkOrderStepStatus.IN_PROGRESS).build();
    private final Location warehouse = Location.builder().id(UUID.randomUUID()).kind(LocationKind.WAREHOUSE).build();
    private final Location hands = Location.builder().id(UUID.randomUUID()).kind(LocationKind.EMPLOYEE).employee(worker).build();
    private final Location consumed = Location.builder().id(UUID.randomUUID()).kind(LocationKind.V_CONSUMED).build();

    private WorkOrderComponent component(String issued, String used, String scrapped) {
        WorkOrderComponent component = WorkOrderComponent.builder().id(UUID.randomUUID()).workOrder(workOrder).step(step)
                .item(screw).qtyRequired(BigDecimal.TEN).qtyIssued(new BigDecimal(issued))
                .qtyConsumed(new BigDecimal(used)).qtyScrapped(new BigDecimal(scrapped)).build();
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        return component;
    }

    private MaterialMovementRequest request(Location from, Location to, String qty) {
        return MaterialMovementRequest.builder()
                .fromLocationId(from != null ? from.getId() : null)
                .toLocationId(to != null ? to.getId() : null)
                .qty(new BigDecimal(qty)).build();
    }

    @Test
    void issueDefaultsToAssigneeAndDrawsDownReservations() {
        step.setAssignee(worker);
        WorkOrderComponent line = component("0", "0", "0");
        when(locationService.getForEmployee(worker.getId())).thenReturn(hands);
        when(componentRepository.save(any(WorkOrderComponent.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderComponent issued = service.issue(line.getId(), request(warehouse, null, "4"));

        ArgumentCaptor<StockMovementRequest> posted = ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(movementService).record(posted.capture());
        assertEquals(MovementType.ISSUE_TO_EMPLOYEE, posted.getValue().getMovementType());
        assertEquals(hands.getId(), posted.getValue().getToLocationId());
        assertEquals(workOrder.getId(), posted.getValue().getWorkOrderId());
        assertEquals(0, new BigDecimal("4").compareTo(issued.getQtyIssued()));
        verify(reservationService).drawDown(line.getId(), new BigDecimal("4"));
    }

    @Test
    void issueNeedsDestinationWhenStepUnassigned() {
        WorkOrderComponent line = component("0", "0", "0");
        assertThrows(ValidationException.class, () -> service.issue(line.getId(), request(warehouse, null, "1")));
        verify(movementService, never()).record(any());
    }

    @Test
    void consumeUpdatesCounter() {
        WorkOrderComponent line = component("5", "1", "0");
        when(locationService.getVirtual(LocationKind.V_CONSUMED)).thenReturn(consumed);
        when(componentRepository.save(any(WorkOrderComponent.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderComponent result = service.consume(line.getId(), request(hands, null, "4"));

        assertEquals(0, new BigDecimal("5").compareTo(result.getQtyConsumed()));
    }

    @Test
    void cannotUseMoreThanHeld() {
        WorkOrderComponent line = component("5", "3", "1");
        assertThrows(ConflictException.class, () -> service.consume(line.getId(), request(hands, null, "2")));
        verify(movementService, never()).record(any());
    }

    @Test
    void returnLowersIssued() {
        WorkOrderComponent line = component("5", "2", "0");
        when(locationService.get(hands.getId())).thenReturn(hands);
        when(componentRepository.save(any(WorkOrderComponent.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderComponent result = service.returnUnused(line.getId(), request(hands, warehouse, "3"));

        ArgumentCaptor<StockMovementRequest> posted = ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(movementService).record(posted.capture());
        assertEquals(MovementType.RETURN_FROM_EMPLOYEE, posted.getValue().getMovementType());
        assertEquals(0, new BigDecimal("2").compareTo(result.getQtyIssued()));
    }

    @Test
    void closedWorkOrderRejectsMaterialActions() {
        workOrder.setStatus(WorkOrderStatus.CANCELLED);
        WorkOrderComponent line = component("5", "0", "0");
        assertThrows(ConflictException.class, () -> service.consume(line.getId(), request(hands, null, "1")));
    }

    @Test
    void produceCappedByPlannedOutput() {
        step.setOutputItem(screw);
        step.setOutputQuantity(BigDecimal.TEN);
        when(stepRepository.findById(step.getId())).thenReturn(Optional.of(step));
        when(movementRepository.sumForStep(step.getId(), MovementType.PRODUCE)).thenReturn(new BigDecimal("8"));

        assertThrows(ConflictException.class, () -> service.produce(step.getId(), request(null, warehouse, "3")));
    }

    @Test
    void produceRequiresStartedStep() {
        step.setOutputItem(screw);
        step.setOutputQuantity(BigDecimal.TEN);
        step.setStatus(WorkOrderStepStatus.READY);
        when(stepRepository.findById(step.getId())).thenReturn(Optional.of(step));

        assertThrows(ConflictException.class, () -> service.produce(step.getId(), request(null, warehouse, "1")));
    }

    @Test
    void producePostsFromConsumed() {
        step.setOutputItem(screw);
        step.setOutputQuantity(BigDecimal.TEN);
        when(stepRepository.findById(step.getId())).thenReturn(Optional.of(step));
        when(movementRepository.sumForStep(step.getId(), MovementType.PRODUCE)).thenReturn(BigDecimal.ZERO);
        when(locationService.getVirtual(LocationKind.V_CONSUMED)).thenReturn(consumed);

        service.produce(step.getId(), request(null, warehouse, "10"));

        ArgumentCaptor<StockMovementRequest> posted = ArgumentCaptor.forClass(StockMovementRequest.class);
        verify(movementService).record(posted.capture());
        assertEquals(MovementType.PRODUCE, posted.getValue().getMovementType());
        assertEquals(consumed.getId(), posted.getValue().getFromLocationId());
        assertEquals(step.getId(), posted.getValue().getStepId());
    }
}
