package com.github.rrin.production;

public enum WorkOrderStepStatus {
    PLANNED,
    READY,
    IN_PROGRESS,
    BLOCKED,
    DONE,
    CANCELLED;

    public boolean isTerminal() {
        return this == DONE || this == CANCELLED;
    }
}
