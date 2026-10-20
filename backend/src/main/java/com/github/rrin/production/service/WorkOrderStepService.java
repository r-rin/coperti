package com.github.rrin.production.service;

import com.github.rrin.production.WorkOrderStep;
import com.github.rrin.production.dto.StepAssignRequest;
import com.github.rrin.production.dto.StepStatusRequest;
import com.github.rrin.production.dto.WorkOrderStepRequest;

import java.util.List;
import java.util.UUID;

public interface WorkOrderStepService {
    WorkOrderStep get(UUID id);
    List<WorkOrderStep> getAllForWorkOrder(UUID workOrderId);
    List<WorkOrderStep> getSubtasks(UUID parentStepId);

    WorkOrderStep addSubtask(WorkOrderStepRequest request);

    /** Only scheduling fields (planned start, deadline) can change after release. */
    WorkOrderStep reschedule(WorkOrderStepRequest request);

    /** Null employee id unassigns the step. */
    WorkOrderStep assign(UUID stepId, StepAssignRequest request);
    WorkOrderStep changeStatus(UUID stepId, StepStatusRequest request);
}
