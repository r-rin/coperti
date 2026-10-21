package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.inventory.ReservationStatus;
import com.github.rrin.inventory.StockReservation;
import com.github.rrin.inventory.dto.StockReservationRequest;
import com.github.rrin.inventory.repository.StockReservationRepository;
import com.github.rrin.inventory.service.ShortageLine;
import com.github.rrin.inventory.service.StockBalanceService;
import com.github.rrin.inventory.service.implementation.StockReservationServiceImpl;
import com.github.rrin.item.Item;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderComponent;
import com.github.rrin.production.WorkOrderStatus;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReservationServiceImplTest {

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderComponentRepository componentRepository;

    @Mock
    private StockBalanceService balanceService;

    @InjectMocks
    private StockReservationServiceImpl service;

    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final WorkOrder workOrder = WorkOrder.builder().id(UUID.randomUUID()).code("WO-1").status(WorkOrderStatus.RELEASED).build();

    private WorkOrderComponent component(String required, String issued) {
        return WorkOrderComponent.builder().id(UUID.randomUUID()).workOrder(workOrder).item(screw)
                .qtyRequired(new BigDecimal(required)).qtyIssued(new BigDecimal(issued)).build();
    }

    private void stubComponent(WorkOrderComponent component, String alreadyReserved) {
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(reservationRepository.sumForComponent(component.getId(), ReservationStatus.RESERVED))
                .thenReturn(new BigDecimal(alreadyReserved));
    }

    @Test
    void reserveWithinNeedAndAvailability() {
        WorkOrderComponent line = component("10", "0");
        stubComponent(line, "0");
        when(balanceService.available(screw.getId())).thenReturn(new BigDecimal("20"));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        StockReservation reservation = service.reserve(new StockReservationRequest(line.getId(), new BigDecimal("5")));

        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
        assertEquals(workOrder, reservation.getWorkOrder());
    }

    @Test
    void reserveBeyondLineNeedRejected() {
        WorkOrderComponent line = component("10", "4");
        stubComponent(line, "3");
        assertThrows(ConflictException.class,
                () -> service.reserve(new StockReservationRequest(line.getId(), new BigDecimal("4"))));
    }

    @Test
    void reserveBeyondAvailabilityRejected() {
        WorkOrderComponent line = component("10", "0");
        stubComponent(line, "0");
        when(balanceService.available(screw.getId())).thenReturn(new BigDecimal("3"));
        assertThrows(ConflictException.class,
                () -> service.reserve(new StockReservationRequest(line.getId(), new BigDecimal("5"))));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserveForClosedWorkOrderRejected() {
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        WorkOrderComponent line = component("10", "0");
        when(componentRepository.findById(line.getId())).thenReturn(Optional.of(line));
        assertThrows(ConflictException.class,
                () -> service.reserve(new StockReservationRequest(line.getId(), BigDecimal.ONE)));
    }

    @Test
    void reserveAllTakesWhatIsAvailable() {
        WorkOrderComponent short_ = component("10", "0");
        WorkOrderComponent none = component("5", "0");
        Item other = Item.builder().id(UUID.randomUUID()).build();
        none.setItem(other);
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(componentRepository.findAllByWorkOrder_Id(workOrder.getId())).thenReturn(List.of(short_, none));
        when(reservationRepository.sumForComponent(any(), any())).thenReturn(BigDecimal.ZERO);
        when(balanceService.available(screw.getId())).thenReturn(new BigDecimal("4"));
        when(balanceService.available(other.getId())).thenReturn(BigDecimal.ZERO);
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        List<StockReservation> created = service.reserveAll(workOrder.getId());

        assertEquals(1, created.size());
        assertEquals(0, new BigDecimal("4").compareTo(created.get(0).getQty()));
    }

    @Test
    void shortageNetsReservedAndFreeStock() {
        WorkOrderComponent line = component("10", "2");
        when(workOrderRepository.findById(workOrder.getId())).thenReturn(Optional.of(workOrder));
        when(componentRepository.findAllByWorkOrder_Id(workOrder.getId())).thenReturn(List.of(line));
        when(reservationRepository.sumForComponent(line.getId(), ReservationStatus.RESERVED)).thenReturn(new BigDecimal("3"));
        when(balanceService.available(screw.getId())).thenReturn(BigDecimal.ONE);

        ShortageLine shortage = service.shortage(workOrder.getId()).get(0);

        assertEquals(0, new BigDecimal("8").compareTo(shortage.outstanding()));
        assertEquals(0, new BigDecimal("3").compareTo(shortage.reserved()));
        assertEquals(0, BigDecimal.ONE.compareTo(shortage.freeAvailable()));
        assertEquals(0, new BigDecimal("4").compareTo(shortage.missing()));
    }

    @Test
    void drawDownUsesOldestReservationFirst() {
        UUID lineId = UUID.randomUUID();
        StockReservation oldest = StockReservation.builder().qty(new BigDecimal("3")).status(ReservationStatus.RESERVED)
                .createdAt(Instant.EPOCH).build();
        StockReservation newer = StockReservation.builder().qty(new BigDecimal("5")).status(ReservationStatus.RESERVED)
                .createdAt(Instant.now()).build();
        when(reservationRepository.findAllByWorkOrderComponent_IdAndStatusOrderByCreatedAtAsc(lineId, ReservationStatus.RESERVED))
                .thenReturn(List.of(oldest, newer));

        service.drawDown(lineId, new BigDecimal("4"));

        assertEquals(ReservationStatus.CONSUMED, oldest.getStatus());
        assertEquals(ReservationStatus.RESERVED, newer.getStatus());
        assertEquals(0, new BigDecimal("4").compareTo(newer.getQty()));
        ArgumentCaptor<StockReservation> saved = ArgumentCaptor.forClass(StockReservation.class);
        verify(reservationRepository, org.mockito.Mockito.times(2)).save(saved.capture());
    }
}
