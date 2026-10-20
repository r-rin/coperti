package com.github.rrin.production.service.implementation;

import com.github.rrin.identity.Employee;
import com.github.rrin.production.StepStatusHistory;
import com.github.rrin.production.WorkOrderStep;
import com.github.rrin.production.WorkOrderStepStatus;
import com.github.rrin.production.repository.StepStatusHistoryRepository;
import com.github.rrin.production.repository.WorkOrderStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * The single place a step's status is written, so the live column and its history row can never drift apart.
 * Shared by the step and work order services; transition rules are checked by callers before recording.
 */
@Component
public class StepStatusRecorder {

    private final WorkOrderStepRepository stepRepository;
    private final StepStatusHistoryRepository historyRepository;

    @Autowired
    public StepStatusRecorder(WorkOrderStepRepository stepRepository, StepStatusHistoryRepository historyRepository) {
        this.stepRepository = stepRepository;
        this.historyRepository = historyRepository;
    }

    public WorkOrderStep record(WorkOrderStep step, WorkOrderStepStatus to, Employee changedBy) {
        WorkOrderStepStatus from = step.getStatus();
        Instant now = Instant.now();

        step.setStatus(to);
        // first start only: resuming after BLOCKED keeps the original start for cycle-time analytics
        if (to == WorkOrderStepStatus.IN_PROGRESS && step.getStartedAt() == null) {
            step.setStartedAt(now);
        }
        if (to.isTerminal()) {
            step.setCompletedAt(now);
        }
        WorkOrderStep saved = stepRepository.save(step);

        historyRepository.save(StepStatusHistory.builder()
                .step(saved)
                .fromStatus(from)
                .toStatus(to)
                .changedAt(now)
                .changedBy(changedBy)
                .build());
        return saved;
    }
}
