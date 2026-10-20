package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.production.WorkOrder;

final class WorkOrderGuard {

    private WorkOrderGuard() {
    }

    // COMPLETED and CANCELLED work orders are records of what happened; their steps are frozen with them
    static void requireOpen(WorkOrder workOrder, String action) {
        new ValidationCheck()
                .check(workOrder.getStatus().isOpen(),
                        "Cannot " + action + ": work order " + workOrder.getCode() + " is " + workOrder.getStatus())
                .throwIfAny(ConflictException::new);
    }
}
