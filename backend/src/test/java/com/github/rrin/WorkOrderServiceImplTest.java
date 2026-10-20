package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.Item;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.repository.ProcessComponentRepository;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.production.*;
import com.github.rrin.production.dto.WorkOrderRequest;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.service.ProductionOrderService;
import com.github.rrin.production.service.implementation.StepStatusRecorder;
import com.github.rrin.production.service.implementation.WorkOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderStepRepository stepRepository;

    @Mock
    private WorkOrderComponentRepository componentRepository;

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private ProcessComponentRepository processComponentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProductionOrderService productionOrderService;

    @Mock
    private StepStatusRecorder statusRecorder;

    @InjectMocks
    private WorkOrderServiceImpl service;

    private final ItemCategory motors = ItemCategory.builder().id(UUID.randomUUID()).name("motor").build();
    private final ItemCategory motors24 = ItemCategory.builder().id(UUID.randomUUID()).name("motor 24V").parent(motors).build();
    private final Item product = Item.builder().id(UUID.randomUUID()).name("pump").build();
    private final Item screw = Item.builder().id(UUID.randomUUID()).name("screw").build();
    private final Item motor24 = Item.builder().id(UUID.randomUUID()).name("motor X 24V").category(motors24).build();

    private ProcessStep first;
    private ProcessStep second;
    private ProcessComponent screwLine;
    private ProcessComponent motorLine;
    private Process process;

    @BeforeEach
    void setUp() {
        first = ProcessStep.builder().id(UUID.randomUUID()).seq(1).build();
        second = ProcessStep.builder().id(UUID.randomUUID()).seq(2).outputItem(product).outputQuantity(1).build();
        screwLine = ProcessComponent.builder().id(UUID.randomUUID()).processStep(first).consumed(screw).consumedQuantity(4).build();
        motorLine = ProcessComponent.builder().id(UUID.randomUUID()).processStep(second).consumableCategory(motors).consumedQuantity(1).build();
        // steps deliberately out of order: release must follow seq, not list order
        process = Process.builder().id(UUID.randomUUID()).produces(product).status(ProcessStatus.ACTIVE)
                .steps(List.of(second, first)).build();
    }

    private WorkOrderRequest request(Map<UUID, UUID> substitutes) {
        return WorkOrderRequest.builder().code("WO-1").processId(process.getId())
                .quantity(new BigDecimal("10")).substitutes(substitutes).build();
    }

    private void stubBom() {
        when(workOrderRepository.existsByCode("WO-1")).thenReturn(false);
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        when(processComponentRepository.findAllByProcessStep_Id(first.getId())).thenReturn(List.of(screwLine));
        when(processComponentRepository.findAllByProcessStep_Id(second.getId())).thenReturn(List.of(motorLine));
    }

    @Test
    void releaseFreezesStepsAndResolvesSubstitutes() {
        stubBom();
        when(itemRepository.findById(motor24.getId())).thenReturn(Optional.of(motor24));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(stepRepository.save(any(WorkOrderStep.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrder released = service.release(request(Map.of(motorLine.getId(), motor24.getId())));

        assertEquals(WorkOrderStatus.RELEASED, released.getStatus());
        assertEquals(WorkOrderKind.PRODUCTION, released.getKind());
        assertEquals(product, released.getItem());

        ArgumentCaptor<WorkOrderStep> steps = ArgumentCaptor.forClass(WorkOrderStep.class);
        verify(stepRepository, times(2)).save(steps.capture());
        assertEquals(1, steps.getAllValues().get(0).getSeq());
        assertEquals(WorkOrderStepStatus.READY, steps.getAllValues().get(0).getStatus());
        assertEquals(WorkOrderStepStatus.PLANNED, steps.getAllValues().get(1).getStatus());
        assertEquals(0, new BigDecimal("10").compareTo(steps.getAllValues().get(1).getOutputQuantity()));

        ArgumentCaptor<WorkOrderComponent> components = ArgumentCaptor.forClass(WorkOrderComponent.class);
        verify(componentRepository, times(2)).save(components.capture());
        assertEquals(screw, components.getAllValues().get(0).getItem());
        assertEquals(0, new BigDecimal("40").compareTo(components.getAllValues().get(0).getQtyRequired()));
        assertEquals(motor24, components.getAllValues().get(1).getItem());
    }

    @Test
    void releaseRequiresSubstituteForEveryCategoryLine() {
        stubBom();
        assertThrows(ValidationException.class, () -> service.release(request(null)));
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void releaseRejectsSubstituteOutsideCategory() {
        stubBom();
        when(itemRepository.findById(screw.getId())).thenReturn(Optional.of(screw));
        assertThrows(ValidationException.class,
                () -> service.release(request(Map.of(motorLine.getId(), screw.getId()))));
    }

    @Test
    void releaseRejectsSubstituteForConcreteLine() {
        stubBom();
        assertThrows(ValidationException.class, () -> service.release(request(Map.of(
                motorLine.getId(), motor24.getId(),
                screwLine.getId(), screw.getId()))));
    }

    @Test
    void releaseRejectsInactiveProcess() {
        process.setStatus(ProcessStatus.DRAFT);
        when(workOrderRepository.existsByCode("WO-1")).thenReturn(false);
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        assertThrows(ConflictException.class, () -> service.release(request(Map.of())));
    }

    @Test
    void releaseRejectsDraftProductionOrder() {
        when(workOrderRepository.existsByCode("WO-1")).thenReturn(false);
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        UUID orderId = UUID.randomUUID();
        when(productionOrderService.get(orderId)).thenReturn(ProductionOrder.builder().id(orderId).code("PO-1")
                .product(product).status(ProductionOrderStatus.DRAFT).build());

        WorkOrderRequest request = request(Map.of());
        request.setProductionOrderId(orderId);
        assertThrows(ConflictException.class, () -> service.release(request));
    }

    @Test
    void releaseByItemUsesActiveProcess() {
        when(workOrderRepository.existsByCode("WO-1")).thenReturn(false);
        when(processRepository.findFirstByProduces_IdAndStatus(product.getId(), ProcessStatus.ACTIVE))
                .thenReturn(Optional.empty());
        WorkOrderRequest request = WorkOrderRequest.builder().code("WO-1").itemId(product.getId())
                .quantity(BigDecimal.ONE).build();
        assertThrows(ConflictException.class, () -> service.release(request));
    }

    @Test
    void completeRequiresAllStepsFinished() {
        UUID id = UUID.randomUUID();
        when(workOrderRepository.findById(id)).thenReturn(Optional.of(
                WorkOrder.builder().id(id).code("WO-1").status(WorkOrderStatus.IN_PROGRESS).build()));
        when(stepRepository.findAllByWorkOrder_IdOrderBySeqAsc(id)).thenReturn(List.of(
                WorkOrderStep.builder().status(WorkOrderStepStatus.DONE).build(),
                WorkOrderStep.builder().status(WorkOrderStepStatus.IN_PROGRESS).build()));
        assertThrows(ConflictException.class, () -> service.complete(id));
    }

    @Test
    void cancelCancelsOpenStepsOnly() {
        UUID id = UUID.randomUUID();
        when(workOrderRepository.findById(id)).thenReturn(Optional.of(
                WorkOrder.builder().id(id).code("WO-1").status(WorkOrderStatus.RELEASED).build()));
        WorkOrderStep done = WorkOrderStep.builder().status(WorkOrderStepStatus.DONE).build();
        WorkOrderStep ready = WorkOrderStep.builder().status(WorkOrderStepStatus.READY).build();
        when(stepRepository.findAllByWorkOrder_IdOrderBySeqAsc(id)).thenReturn(List.of(done, ready));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(WorkOrderStatus.CANCELLED, service.cancel(id, null).getStatus());
        verify(statusRecorder).record(ready, WorkOrderStepStatus.CANCELLED, null);
        verify(statusRecorder, never()).record(done, WorkOrderStepStatus.CANCELLED, null);
    }

    @Test
    void markInProgressPropagatesToProductionOrder() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(workOrderRepository.findById(id)).thenReturn(Optional.of(WorkOrder.builder().id(id)
                .order(ProductionOrder.builder().id(orderId).build()).status(WorkOrderStatus.RELEASED).build()));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(WorkOrderStatus.IN_PROGRESS, service.markInProgress(id).getStatus());
        verify(productionOrderService).markInProgress(orderId);
    }
}
