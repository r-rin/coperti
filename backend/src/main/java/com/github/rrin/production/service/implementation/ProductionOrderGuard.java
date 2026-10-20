package com.github.rrin.production.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.production.ProductionOrder;
import com.github.rrin.production.ProductionOrderStatus;

import java.util.Arrays;

final class ProductionOrderGuard {

    private ProductionOrderGuard() {
    }

    static void requireStatus(ProductionOrder order, String action, ProductionOrderStatus... allowed) {
        new ValidationCheck()
                .check(Arrays.asList(allowed).contains(order.getStatus()),
                        "Cannot " + action + ": production order " + order.getCode() + " is " + order.getStatus()
                                + ", expected one of " + Arrays.toString(allowed))
                .throwIfAny(ConflictException::new);
    }

    // a batch can only be closed once nothing is still being built against it
    static void requireNoOpenWorkOrders(ProductionOrder order, String action, boolean hasOpenWorkOrders) {
        new ValidationCheck()
                .check(!hasOpenWorkOrders,
                        "Cannot " + action + ": production order " + order.getCode()
                                + " still has released or in-progress work orders")
                .throwIfAny(ConflictException::new);
    }
}
