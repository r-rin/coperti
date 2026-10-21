package com.github.rrin.inventory.dto;

import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.QualityState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequest {
    private UUID itemId;
    private BigDecimal qty;
    private UUID fromLocationId;
    private UUID toLocationId;
    // both default to GOOD
    private QualityState fromQuality;
    private QualityState toQuality;
    private MovementType movementType;
    // defaults to now
    private Instant occurredAt;
    private UUID performedById;
    private UUID workOrderId;
    private UUID stepId;
    private UUID otherUseId;
    private String note;
}
