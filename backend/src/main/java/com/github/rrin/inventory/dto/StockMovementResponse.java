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
public class StockMovementResponse {
    private UUID id;
    private UUID itemId;
    private BigDecimal qty;
    private UUID fromLocationId;
    private UUID toLocationId;
    private QualityState fromQuality;
    private QualityState toQuality;
    private MovementType movementType;
    private Instant occurredAt;
    private UUID performedById;
    private UUID workOrderId;
    private UUID stepId;
    private UUID otherUseId;
    private UUID reversalOfId;
    private String note;
}
