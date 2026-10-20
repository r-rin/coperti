package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.production.WorkOrderStep;
import com.github.rrin.production.WorkOrderStepStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.github.rrin.production.WorkOrderStepStatus.*;

final class StepTransitions {

    private static final Map<WorkOrderStepStatus, Set<WorkOrderStepStatus>> ALLOWED = new EnumMap<>(WorkOrderStepStatus.class);

    static {
        ALLOWED.put(PLANNED, EnumSet.of(READY, BLOCKED, CANCELLED));
        ALLOWED.put(READY, EnumSet.of(PLANNED, IN_PROGRESS, BLOCKED, CANCELLED));
        ALLOWED.put(IN_PROGRESS, EnumSet.of(BLOCKED, DONE, CANCELLED));
        ALLOWED.put(BLOCKED, EnumSet.of(READY, IN_PROGRESS, CANCELLED));
        // DONE and CANCELLED are final: a finished step is history, redoing it is a new rework work order
        ALLOWED.put(DONE, EnumSet.noneOf(WorkOrderStepStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(WorkOrderStepStatus.class));
    }

    private StepTransitions() {
    }

    static void requireAllowed(WorkOrderStep step, WorkOrderStepStatus target) {
        new ValidationCheck()
                .check(ALLOWED.get(step.getStatus()).contains(target),
                        "Cannot move step " + step.getId() + " from " + step.getStatus() + " to " + target)
                .throwIfAny(ConflictException::new);
    }

    static void requireNotFinished(WorkOrderStep step, String action) {
        new ValidationCheck()
                .check(!step.getStatus().isTerminal(),
                        "Cannot " + action + ": step " + step.getId() + " is already " + step.getStatus())
                .throwIfAny(ConflictException::new);
    }
}
