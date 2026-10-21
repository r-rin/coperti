package com.github.rrin.inventory;

public enum MovementType {
    RECEIPT,
    ISSUE_TO_EMPLOYEE,
    RETURN_FROM_EMPLOYEE,
    CONSUME,
    PRODUCE,
    TRANSFER,
    QUALITY_CHANGE,
    SCRAP,
    RETURN_TO_SUPPLIER,
    OTHER_USE,
    RECOVER_FROM_TEARDOWN,
}
