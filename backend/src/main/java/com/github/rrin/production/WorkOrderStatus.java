package com.github.rrin.production;

public enum WorkOrderStatus {
    RELEASED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean isOpen() {
        return this == RELEASED || this == IN_PROGRESS;
    }
}
