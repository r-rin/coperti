package com.github.rrin.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortageResponse {
    private UUID itemId;
    private BigDecimal outstanding;
    private BigDecimal reserved;
    private BigDecimal freeAvailable;
    private BigDecimal missing;
}
