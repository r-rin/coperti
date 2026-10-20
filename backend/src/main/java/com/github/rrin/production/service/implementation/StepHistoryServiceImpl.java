package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.production.StepAssignment;
import com.github.rrin.production.StepStatusHistory;
import com.github.rrin.production.repository.StepAssignmentRepository;
import com.github.rrin.production.repository.StepStatusHistoryRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import com.github.rrin.production.service.StepHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StepHistoryServiceImpl implements StepHistoryService {

    private final StepAssignmentRepository assignmentRepository;
    private final StepStatusHistoryRepository statusHistoryRepository;
    private final WorkOrderStepRepository stepRepository;

    @Autowired
    public StepHistoryServiceImpl(StepAssignmentRepository assignmentRepository,
                                  StepStatusHistoryRepository statusHistoryRepository,
                                  WorkOrderStepRepository stepRepository) {
        this.assignmentRepository = assignmentRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.stepRepository = stepRepository;
    }

    @Override
    public List<StepAssignment> getAssignmentsForStep(UUID stepId) {
        requireStep(stepId);
        return assignmentRepository.findAllByStep_IdOrderByAssignedAtAsc(stepId);
    }

    @Override
    public List<StepAssignment> getAssignmentsForEmployee(UUID employeeId, Instant from, Instant to) {
        new ValidationCheck()
                .check(employeeId != null, "Employee id is required")
                .check(from == null || to == null || !to.isBefore(from), "End of period cannot be before its start")
                .throwIfAny(InvalidQuery::new);
        return assignmentRepository.findOverlapping(employeeId,
                from != null ? from : Instant.EPOCH,
                to != null ? to : Instant.now());
    }

    @Override
    public List<StepStatusHistory> getStatusHistoryForStep(UUID stepId) {
        requireStep(stepId);
        return statusHistoryRepository.findAllByStep_IdOrderByChangedAtAsc(stepId);
    }

    private void requireStep(UUID stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new EntityNotFoundException("Work order step with id " + stepId + " not found");
        }
    }
}
