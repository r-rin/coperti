package com.github.rrin.inventory.dto;

import com.github.rrin.inventory.QualityState;
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
public class OtherUseLineResponse {
    private UUID id;
    private UUID itemId;
    private UUID fromLocationId;
    private QualityState quality;
    private BigDecimal qty;
}
