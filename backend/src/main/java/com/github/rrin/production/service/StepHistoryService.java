package com.github.rrin.production.service;

import com.github.rrin.production.StepAssignment;
import com.github.rrin.production.StepStatusHistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read side of the step audit trail; rows are written by WorkOrderStepService as steps change. */
public interface StepHistoryService {
    List<StepAssignment> getAssignmentsForStep(UUID stepId);

    /** Assignments of the employee that overlap the period; null bounds are open. */
    List<StepAssignment> getAssignmentsForEmployee(UUID employeeId, Instant from, Instant to);

    List<StepStatusHistory> getStatusHistoryForStep(UUID stepId);
}
