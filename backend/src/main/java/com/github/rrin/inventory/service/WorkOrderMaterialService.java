package com.github.rrin.inventory.service;

import com.github.rrin.inventory.StockMovement;
import com.github.rrin.inventory.dto.MaterialMovementRequest;
import com.github.rrin.production.WorkOrderComponent;

import java.util.UUID;

/** Moves material for a work order and keeps its component counters (issued / consumed / scrapped) in step with the ledger. */
public interface WorkOrderMaterialService {
    WorkOrderComponent issue(UUID componentId, MaterialMovementRequest request);
    WorkOrderComponent consume(UUID componentId, MaterialMovementRequest request);
    WorkOrderComponent scrap(UUID componentId, MaterialMovementRequest request);
    WorkOrderComponent returnUnused(UUID componentId, MaterialMovementRequest request);
    StockMovement produce(UUID stepId, MaterialMovementRequest request);
}
