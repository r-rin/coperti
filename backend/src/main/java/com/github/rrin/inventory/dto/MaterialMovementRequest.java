package com.github.rrin.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Body of the work order material actions. issue: from (stock) → to (defaults to the step assignee);
 * consume/scrap: from = where the material is held; return: from → to; produce: to only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMovementRequest {
    private UUID fromLocationId;
    private UUID toLocationId;
    private BigDecimal qty;
    private UUID performedById;
    private String note;
}
