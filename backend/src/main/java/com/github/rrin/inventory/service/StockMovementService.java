package com.github.rrin.inventory.service;

import com.github.rrin.inventory.StockMovement;
import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.dto.StockReversalRequest;
import com.github.rrin.inventory.dto.filter.StockMovementFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

/** The only writer of the stock ledger; every row it saves also updates the balance cache in the same transaction. */
public interface StockMovementService {
    StockMovement get(UUID id);
    Page<StockMovement> search(StockMovementFilter filter, int page, int size);

    /**
     * Manual entry from the API: receipts, transfers, quality changes, supplier returns, scrap and plain issues/returns.
     * Movements that other bookkeeping depends on (work order material, other use) must go through their own services.
     */
    StockMovement post(StockMovementRequest request);

    /** Internal entry point for the services that own consumption, production, other use and recovery. */
    StockMovement record(StockMovementRequest request);

    /** Posts the compensating row; the original stays untouched. */
    StockMovement reverse(UUID id, StockReversalRequest request);
}
