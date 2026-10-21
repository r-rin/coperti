package com.github.rrin.inventory;

public enum LocationKind {
    WAREHOUSE,
    EMPLOYEE,
    WIP,
    REPAIR,
    QUARANTINE,
    FINISHED_GOODS,
    // virtual sinks and sources: they exist so every movement balances, and hold no real stock
    V_SUPPLIER,
    V_CONSUMED,
    V_SCRAP,
    V_OTHER_USE;

    public boolean isVirtual() {
        return name().startsWith("V_");
    }
}
