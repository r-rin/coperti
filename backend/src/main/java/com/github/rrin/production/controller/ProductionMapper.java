package com.github.rrin.production.controller;

import com.github.rrin.production.*;
import com.github.rrin.production.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ProductionMapper {

    public ProductionOrderResponse toResponse(ProductionOrder order) {
        return ProductionOrderResponse.builder()
                .id(order.getId())
                .code(order.getCode())
                .productId(order.getProduct() != null ? order.getProduct().getId() : null)
                .quantity(order.getQuantity())
                .dueDate(order.getDueDate())
                .description(order.getDescription())
                .responsibleId(order.getResponsible() != null ? order.getResponsible().getId() : null)
                .createdById(order.getCreatedBy() != null ? order.getCreatedBy().getId() : null)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public WorkOrderResponse toResponse(WorkOrder workOrder) {
        return WorkOrderResponse.builder()
                .id(workOrder.getId())
                .code(workOrder.getCode())
                .productionOrderId(workOrder.getOrder() != null ? workOrder.getOrder().getId() : null)
                .itemId(workOrder.getItem() != null ? workOrder.getItem().getId() : null)
                .processId(workOrder.getProcess() != null ? workOrder.getProcess().getId() : null)
                .quantity(workOrder.getQuantity())
                .kind(workOrder.getKind())
                .primaryAssigneeId(workOrder.getPrimaryAssignee() != null ? workOrder.getPrimaryAssignee().getId() : null)
                .status(workOrder.getStatus())
                .releasedAt(workOrder.getReleasedAt())
                .completedAt(workOrder.getCompletedAt())
                .build();
    }

    public WorkOrderStepResponse toResponse(WorkOrderStep step) {
        return WorkOrderStepResponse.builder()
                .id(step.getId())
                .workOrderId(step.getWorkOrder() != null ? step.getWorkOrder().getId() : null)
                .parentStepId(step.getParent() != null ? step.getParent().getId() : null)
                .seq(step.getSeq())
                .operationId(step.getOperation() != null ? step.getOperation().getId() : null)
                .outputItemId(step.getOutputItem() != null ? step.getOutputItem().getId() : null)
                .outputQuantity(step.getOutputQuantity())
                .assigneeId(step.getAssignee() != null ? step.getAssignee().getId() : null)
                .plannedStart(step.getPlannedStart())
                .deadline(step.getDeadline())
                .status(step.getStatus())
                .startedAt(step.getStartedAt())
                .completedAt(step.getCompletedAt())
                .build();
    }

    public WorkOrderComponentResponse toResponse(WorkOrderComponent component) {
        return WorkOrderComponentResponse.builder()
                .id(component.getId())
                .workOrderId(component.getWorkOrder() != null ? component.getWorkOrder().getId() : null)
                .stepId(component.getStep() != null ? component.getStep().getId() : null)
                .itemId(component.getItem() != null ? component.getItem().getId() : null)
                .qtyRequired(component.getQtyRequired())
                .qtyIssued(component.getQtyIssued())
                .qtyConsumed(component.getQtyConsumed())
                .qtyScrapped(component.getQtyScrapped())
                .build();
    }

    public StepAssignmentResponse toResponse(StepAssignment assignment) {
        return StepAssignmentResponse.builder()
                .id(assignment.getId())
                .stepId(assignment.getStep() != null ? assignment.getStep().getId() : null)
                .employeeId(assignment.getEmployee() != null ? assignment.getEmployee().getId() : null)
                .assignedAt(assignment.getAssignedAt())
                .unassignedAt(assignment.getUnassignedAt())
                .assignedById(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null)
                .build();
    }

    public StepStatusHistoryResponse toResponse(StepStatusHistory history) {
        return StepStatusHistoryResponse.builder()
                .id(history.getId())
                .stepId(history.getStep() != null ? history.getStep().getId() : null)
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedAt(history.getChangedAt())
                .changedById(history.getChangedBy() != null ? history.getChangedBy().getId() : null)
                .build();
    }
}
