package com.github.rrin.inventory.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.production.WorkOrder;

import java.math.BigDecimal;

final class InventoryGuard {

    private InventoryGuard() {
    }

    // a finished or cancelled work order's material record is closed along with it
    static void requireOpen(WorkOrder workOrder, String action) {
        new ValidationCheck()
                .check(workOrder.getStatus().isOpen(),
                        "Cannot " + action + ": work order " + workOrder.getCode() + " is " + workOrder.getStatus())
                .throwIfAny(ConflictException::new);
    }

    static void requireWithin(BigDecimal qty, BigDecimal limit, String message) {
        new ValidationCheck()
                .check(qty.compareTo(limit) <= 0, message + " (" + qty.stripTrailingZeros().toPlainString()
                        + " requested, " + limit.stripTrailingZeros().toPlainString() + " allowed)")
                .throwIfAny(ConflictException::new);
    }
}
