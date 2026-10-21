package com.github.rrin.inventory.dto.filter;

import com.github.rrin.inventory.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementFilter {
    private UUID itemId;
    // matches movements on either side of the location
    private UUID locationId;
    private Set<MovementType> types;
    private UUID workOrderId;
    private UUID performedById;
    private Instant from;
    private Instant to;
}
