package com.github.rrin.inventory.dto;

import com.github.rrin.inventory.ReservationStatus;
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
public class StockReservationResponse {
    private UUID id;
    private UUID itemId;
    private UUID workOrderId;
    private UUID workOrderComponentId;
    private BigDecimal qty;
    private ReservationStatus status;
    private Instant createdAt;
}
