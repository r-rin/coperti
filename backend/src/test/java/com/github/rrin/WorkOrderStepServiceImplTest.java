package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Operation;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.production.*;
import com.github.rrin.production.dto.StepAssignRequest;
import com.github.rrin.production.dto.StepStatusRequest;
import com.github.rrin.production.dto.WorkOrderStepRequest;
import com.github.rrin.production.repository.StepAssignmentRepository;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.service.WorkOrderService;
import com.github.rrin.production.service.implementation.StepStatusRecorder;
import com.github.rrin.production.service.implementation.WorkOrderStepServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderStepServiceImplTest {

    @Mock
    private WorkOrderStepRepository stepRepository;

    @Mock
    private WorkOrderComponentRepository componentRepository;

    @Mock
    private StepAssignmentRepository assignmentRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkOrderService workOrderService;

    @Mock
    private StepStatusRecorder statusRecorder;

    @InjectMocks
    private WorkOrderStepServiceImpl service;

    private final WorkOrder workOrder = WorkOrder.builder().id(UUID.randomUUID()).code("WO-1")
            .status(WorkOrderStatus.RELEASED).build();

    private WorkOrderStep step(int seq, WorkOrderStepStatus status) {
        return WorkOrderStep.builder().id(UUID.randomUUID()).workOrder(workOrder).seq(seq).status(status).build();
    }

    private void stubFound(WorkOrderStep step) {
        when(stepRepository.findById(step.getId())).thenReturn(Optional.of(step));
    }

    private void stubRecorder() {
        when(statusRecorder.record(any(), any(), any())).thenAnswer(i -> {
            WorkOrderStep step = i.getArgument(0);
            step.setStatus(i.getArgument(1));
            return step;
        });
    }

    @Test
    void startingStepMarksWorkOrderInProgress() {
        WorkOrderStep step = step(1, WorkOrderStepStatus.READY);
        stubFound(step);
        stubRecorder();

        service.changeStatus(step.getId(), new StepStatusRequest(WorkOrderStepStatus.IN_PROGRESS, null));

        verify(statusRecorder).record(step, WorkOrderStepStatus.IN_PROGRESS, null);
        verify(workOrderService).markInProgress(workOrder.getId());
    }

    @Test
    void illegalTransitionRejected() {
        WorkOrderStep step = step(1, WorkOrderStepStatus.PLANNED);
        stubFound(step);
        assertThrows(ConflictException.class,
                () -> service.changeStatus(step.getId(), new StepStatusRequest(WorkOrderStepStatus.DONE, null)));
        verify(statusRecorder, never()).record(any(), any(), any());
    }

    @Test
    void finishedStepIsFinal() {
        WorkOrderStep step = step(1, WorkOrderStepStatus.DONE);
        stubFound(step);
        assertThrows(ConflictException.class,
                () -> service.changeStatus(step.getId(), new StepStatusRequest(WorkOrderStepStatus.IN_PROGRESS, null)));
    }

    @Test
    void statusRequired() {
        assertThrows(ValidationException.class,
                () -> service.changeStatus(UUID.randomUUID(), new StepStatusRequest(null, null)));
    }

    @Test
    void stepsOfClosedWorkOrderAreFrozen() {
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        WorkOrderStep step = step(1, WorkOrderStepStatus.READY);
        stubFound(step);
        assertThrows(ConflictException.class,
                () -> service.changeStatus(step.getId(), new StepStatusRequest(WorkOrderStepStatus.IN_PROGRESS, null)));
    }

    @Test
    void parentCannotFinishWithOpenSubtask() {
        WorkOrderStep parent = step(1, WorkOrderStepStatus.IN_PROGRESS);
        stubFound(parent);
        when(stepRepository.findAllByParent_IdOrderBySeqAsc(parent.getId()))
                .thenReturn(List.of(step(1, WorkOrderStepStatus.IN_PROGRESS)));
        assertThrows(ConflictException.class,
                () -> service.changeStatus(parent.getId(), new StepStatusRequest(WorkOrderStepStatus.DONE, null)));
    }

    @Test
    void finishingStepReadiesNextPlannedSibling() {
        WorkOrderStep current = step(1, WorkOrderStepStatus.IN_PROGRESS);
        WorkOrderStep next = step(2, WorkOrderStepStatus.PLANNED);
        WorkOrderStep later = step(3, WorkOrderStepStatus.PLANNED);
        stubFound(current);
        stubRecorder();
        when(stepRepository.findAllByParent_IdOrderBySeqAsc(current.getId())).thenReturn(List.of());
        when(stepRepository.findAllByWorkOrder_IdAndParentIsNullOrderBySeqAsc(workOrder.getId()))
                .thenReturn(List.of(current, next, later));

        service.changeStatus(current.getId(), new StepStatusRequest(WorkOrderStepStatus.DONE, null));

        verify(statusRecorder).record(next, WorkOrderStepStatus.READY, null);
        verify(statusRecorder, never()).record(later, WorkOrderStepStatus.READY, null);
    }

    @Test
    void cancellingParentCancelsOpenSubtasks() {
        WorkOrderStep parent = step(1, WorkOrderStepStatus.READY);
        WorkOrderStep openChild = step(1, WorkOrderStepStatus.IN_PROGRESS);
        WorkOrderStep doneChild = step(2, WorkOrderStepStatus.DONE);
        stubFound(parent);
        stubRecorder();
        when(stepRepository.findAllByParent_IdOrderBySeqAsc(parent.getId())).thenReturn(List.of(openChild, doneChild));
        when(stepRepository.findAllByParent_IdOrderBySeqAsc(openChild.getId())).thenReturn(List.of());

        service.changeStatus(parent.getId(), new StepStatusRequest(WorkOrderStepStatus.CANCELLED, null));

        verify(statusRecorder).record(openChild, WorkOrderStepStatus.CANCELLED, null);
        verify(statusRecorder, never()).record(doneChild, WorkOrderStepStatus.CANCELLED, null);
        verify(statusRecorder).record(parent, WorkOrderStepStatus.CANCELLED, null);
    }

    @Test
    void reassignClosesPreviousAssignment() {
        Employee previous = Employee.builder().id(UUID.randomUUID()).build();
        Employee next = Employee.builder().id(UUID.randomUUID()).build();
        WorkOrderStep step = step(1, WorkOrderStepStatus.IN_PROGRESS);
        step.setAssignee(previous);
        StepAssignment open = StepAssignment.builder().step(step).employee(previous).assignedAt(Instant.EPOCH).build();
        stubFound(step);
        when(employeeRepository.findById(next.getId())).thenReturn(Optional.of(next));
        when(assignmentRepository.findFirstByStep_IdAndUnassignedAtIsNull(step.getId())).thenReturn(Optional.of(open));
        when(stepRepository.save(any(WorkOrderStep.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderStep assigned = service.assign(step.getId(), new StepAssignRequest(next.getId(), null));

        assertEquals(next, assigned.getAssignee());
        assertNotNull(open.getUnassignedAt());
        ArgumentCaptor<StepAssignment> saved = ArgumentCaptor.forClass(StepAssignment.class);
        verify(assignmentRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        StepAssignment created = saved.getAllValues().get(1);
        assertEquals(next, created.getEmployee());
        assertNull(created.getUnassignedAt());
    }

    @Test
    void reassigningSameEmployeeIsNoOp() {
        Employee current = Employee.builder().id(UUID.randomUUID()).build();
        WorkOrderStep step = step(1, WorkOrderStepStatus.READY);
        step.setAssignee(current);
        stubFound(step);
        when(employeeRepository.findById(current.getId())).thenReturn(Optional.of(current));

        service.assign(step.getId(), new StepAssignRequest(current.getId(), null));

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void addSubtaskStartsPlannedUnderParent() {
        WorkOrderStep parent = step(1, WorkOrderStepStatus.IN_PROGRESS);
        Operation operation = Operation.builder().id(UUID.randomUUID()).build();
        stubFound(parent);
        when(operationRepository.findById(operation.getId())).thenReturn(Optional.of(operation));
        when(stepRepository.save(any(WorkOrderStep.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderStep subtask = service.addSubtask(WorkOrderStepRequest.builder()
                .parentStepId(parent.getId()).seq(1).operationId(operation.getId()).build());

        assertEquals(parent, subtask.getParent());
        assertEquals(workOrder, subtask.getWorkOrder());
        assertEquals(WorkOrderStepStatus.PLANNED, subtask.getStatus());
    }

    @Test
    void addSubtaskRejectedUnderFinishedParent() {
        WorkOrderStep parent = step(1, WorkOrderStepStatus.DONE);
        stubFound(parent);
        assertThrows(ConflictException.class, () -> service.addSubtask(WorkOrderStepRequest.builder()
                .parentStepId(parent.getId()).seq(1).operationId(UUID.randomUUID()).build()));
    }

    @Test
    void rescheduleRejectsDeadlineBeforeStart() {
        assertThrows(ValidationException.class, () -> service.reschedule(WorkOrderStepRequest.builder()
                .id(UUID.randomUUID())
                .plannedStart(Instant.parse("2026-10-10T00:00:00Z"))
                .deadline(Instant.parse("2026-10-01T00:00:00Z"))
                .build()));
    }
}
