package com.github.rrin.inventory.service;

import com.github.rrin.inventory.StockBalance;
import com.github.rrin.inventory.StockMovement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockBalanceService {

    /** Applies one ledger row to the cache; rejects taking a real location below zero. */
    void apply(StockMovement movement);

    /** Non-zero balances, optionally narrowed by item, location, or the employee's own location. */
    List<StockBalance> find(UUID itemId, UUID locationId, UUID employeeId);

    /** GOOD stock in warehouses minus what open work orders have reserved. Can be negative if stock left after reserving. */
    BigDecimal available(UUID itemId);

    /** Recomputes the whole cache from the ledger. */
    void rebuild();
}
