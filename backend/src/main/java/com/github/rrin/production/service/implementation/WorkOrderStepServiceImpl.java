package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Operation;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.production.*;
import com.github.rrin.production.dto.StepAssignRequest;
import com.github.rrin.production.dto.StepStatusRequest;
import com.github.rrin.production.dto.WorkOrderComponentRequest;
import com.github.rrin.production.dto.WorkOrderStepRequest;
import com.github.rrin.production.repository.StepAssignmentRepository;
import com.github.rrin.production.repository.WorkOrderComponentRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.service.WorkOrderService;
import com.github.rrin.production.service.WorkOrderStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class WorkOrderStepServiceImpl implements WorkOrderStepService {

    private final WorkOrderStepRepository stepRepository;
    private final WorkOrderComponentRepository componentRepository;
    private final StepAssignmentRepository assignmentRepository;
    private final OperationRepository operationRepository;
    private final ItemRepository itemRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkOrderService workOrderService;
    private final StepStatusRecorder statusRecorder;

    @Autowired
    public WorkOrderStepServiceImpl(WorkOrderStepRepository stepRepository,
                                    WorkOrderComponentRepository componentRepository,
                                    StepAssignmentRepository assignmentRepository,
                                    OperationRepository operationRepository,
                                    ItemRepository itemRepository,
                                    EmployeeRepository employeeRepository,
                                    WorkOrderService workOrderService,
                                    StepStatusRecorder statusRecorder) {
        this.stepRepository = stepRepository;
        this.componentRepository = componentRepository;
        this.assignmentRepository = assignmentRepository;
        this.operationRepository = operationRepository;
        this.itemRepository = itemRepository;
        this.employeeRepository = employeeRepository;
        this.workOrderService = workOrderService;
        this.statusRecorder = statusRecorder;
    }

    @Override
    public WorkOrderStep get(UUID id) {
        return getIfExists(id);
    }

    @Override
    public List<WorkOrderStep> getAllForWorkOrder(UUID workOrderId) {
        return stepRepository.findAllByWorkOrder_IdOrderBySeqAsc(workOrderService.get(workOrderId).getId());
    }

    @Override
    public List<WorkOrderStep> getSubtasks(UUID parentStepId) {
        return stepRepository.findAllByParent_IdOrderBySeqAsc(getIfExists(parentStepId).getId());
    }

    @Override
    @Transactional
    public WorkOrderStep addSubtask(WorkOrderStepRequest request) {
        ValidationCheck fields = new ValidationCheck()
                .check(request.getParentStepId() != null, "Parent step id is required")
                .check(request.getOperationId() != null, "Operation id is required")
                .check(request.getOutputItemId() == null
                                || (request.getOutputQuantity() != null && request.getOutputQuantity().compareTo(BigDecimal.ZERO) > 0),
                        "Output quantity must be greater than 0 when an output item is set");
        validateSchedule(fields, request);
        if (request.getComponents() != null) {
            for (WorkOrderComponentRequest component : request.getComponents()) {
                fields.check(component.getItemId() != null, "Component item id is required")
                        .check(component.getQtyRequired() != null && component.getQtyRequired().compareTo(BigDecimal.ZERO) > 0,
                                "Component quantity must be greater than 0");
            }
        }
        fields.throwIfAny(ValidationException::new);

        WorkOrderStep parent = getIfExists(request.getParentStepId());
        WorkOrderGuard.requireOpen(parent.getWorkOrder(), "add subtask");
        StepTransitions.requireNotFinished(parent, "add subtask");

        WorkOrderStep subtask = stepRepository.save(WorkOrderStep.builder()
                .workOrder(parent.getWorkOrder())
                .parent(parent)
                .seq(request.getSeq())
                .operation(getOperation(request.getOperationId()))
                .outputItem(request.getOutputItemId() != null ? getItem(request.getOutputItemId()) : null)
                .outputQuantity(request.getOutputItemId() != null ? request.getOutputQuantity() : null)
                .plannedStart(request.getPlannedStart())
                .deadline(request.getDeadline())
                .status(WorkOrderStepStatus.PLANNED)
                .build());

        if (request.getComponents() != null) {
            for (WorkOrderComponentRequest component : request.getComponents()) {
                componentRepository.save(WorkOrderComponent.builder()
                        .workOrder(parent.getWorkOrder())
                        .step(subtask)
                        .item(getItem(component.getItemId()))
                        .qtyRequired(component.getQtyRequired())
                        .build());
            }
        }
        return subtask;
    }

    @Override
    public WorkOrderStep reschedule(WorkOrderStepRequest request) {
        ValidationCheck fields = new ValidationCheck().check(request.getId() != null, "Step id is required");
        validateSchedule(fields, request);
        fields.throwIfAny(ValidationException::new);

        WorkOrderStep step = getIfExists(request.getId());
        WorkOrderGuard.requireOpen(step.getWorkOrder(), "reschedule step");
        StepTransitions.requireNotFinished(step, "reschedule step");

        step.setPlannedStart(request.getPlannedStart());
        step.setDeadline(request.getDeadline());
        return stepRepository.save(step);
    }

    @Override
    @Transactional
    public WorkOrderStep assign(UUID stepId, StepAssignRequest request) {
        WorkOrderStep step = getIfExists(stepId);
        WorkOrderGuard.requireOpen(step.getWorkOrder(), "assign step");
        StepTransitions.requireNotFinished(step, "assign step");

        Employee employee = getOptionalEmployee(request.getEmployeeId());
        UUID currentId = step.getAssignee() != null ? step.getAssignee().getId() : null;
        if (Objects.equals(currentId, request.getEmployeeId())) {
            return step;
        }

        Instant now = Instant.now();
        assignmentRepository.findFirstByStep_IdAndUnassignedAtIsNull(stepId).ifPresent(current -> {
            current.setUnassignedAt(now);
            assignmentRepository.save(current);
        });
        if (employee != null) {
            assignmentRepository.save(StepAssignment.builder()
                    .step(step)
                    .employee(employee)
                    .assignedAt(now)
                    .assignedBy(getOptionalEmployee(request.getAssignedById()))
                    .build());
        }

        step.setAssignee(employee);
        return stepRepository.save(step);
    }

    @Override
    @Transactional
    public WorkOrderStep changeStatus(UUID stepId, StepStatusRequest request) {
        new ValidationCheck()
                .check(request.getStatus() != null, "Status is required")
                .throwIfAny(ValidationException::new);

        WorkOrderStep step = getIfExists(stepId);
        WorkOrderStepStatus target = request.getStatus();
        WorkOrderGuard.requireOpen(step.getWorkOrder(), "change step status");
        StepTransitions.requireAllowed(step, target);

        List<WorkOrderStep> subtasks = stepRepository.findAllByParent_IdOrderBySeqAsc(stepId);
        if (target == WorkOrderStepStatus.DONE) {
            new ValidationCheck()
                    .check(subtasks.stream().allMatch(subtask -> subtask.getStatus().isTerminal()),
                            "Cannot finish step " + stepId + ": some of its subtasks are not finished")
                    .throwIfAny(ConflictException::new);
        }

        Employee changedBy = getOptionalEmployee(request.getChangedById());
        if (target == WorkOrderStepStatus.CANCELLED) {
            cancelSubtree(subtasks, changedBy);
        }
        WorkOrderStep saved = statusRecorder.record(step, target, changedBy);

        if (target == WorkOrderStepStatus.IN_PROGRESS) {
            workOrderService.markInProgress(step.getWorkOrder().getId());
        }
        if (target == WorkOrderStepStatus.DONE) {
            readyNextSibling(saved, changedBy);
        }
        return saved;
    }

    // a cancelled task takes its open subtasks with it: they only existed to feed it
    private void cancelSubtree(List<WorkOrderStep> subtasks, Employee changedBy) {
        for (WorkOrderStep subtask : subtasks) {
            if (!subtask.getStatus().isTerminal()) {
                cancelSubtree(stepRepository.findAllByParent_IdOrderBySeqAsc(subtask.getId()), changedBy);
                statusRecorder.record(subtask, WorkOrderStepStatus.CANCELLED, changedBy);
            }
        }
    }

    // finishing a step hands the work on: the next planned sibling in sequence becomes ready
    private void readyNextSibling(WorkOrderStep step, Employee changedBy) {
        List<WorkOrderStep> siblings = step.getParent() == null
                ? stepRepository.findAllByWorkOrder_IdAndParentIsNullOrderBySeqAsc(step.getWorkOrder().getId())
                : stepRepository.findAllByParent_IdOrderBySeqAsc(step.getParent().getId());
        siblings.stream()
                .filter(sibling -> sibling.getSeq() > step.getSeq())
                .min(Comparator.comparingInt(WorkOrderStep::getSeq))
                .filter(next -> next.getStatus() == WorkOrderStepStatus.PLANNED)
                .ifPresent(next -> statusRecorder.record(next, WorkOrderStepStatus.READY, changedBy));
    }

    private void validateSchedule(ValidationCheck check, WorkOrderStepRequest request) {
        check.check(request.getPlannedStart() == null || request.getDeadline() == null
                        || !request.getDeadline().isBefore(request.getPlannedStart()),
                "Deadline cannot be before the planned start");
    }

    private Operation getOperation(UUID id) {
        return operationRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Operation with id " + id + " not found"));
    }

    private Item getItem(UUID id) {
        return itemRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Item with id " + id + " not found"));
    }

    private Employee getOptionalEmployee(UUID id) {
        if (id == null) return null;
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }

    private WorkOrderStep getIfExists(UUID id) {
        return stepRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order step with id " + id + " not found"));
    }
}
