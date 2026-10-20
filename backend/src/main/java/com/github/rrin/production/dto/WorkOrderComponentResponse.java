package com.github.rrin.production.dto;

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
public class WorkOrderComponentResponse {
    private UUID id;
    private UUID workOrderId;
    private UUID stepId;
    private UUID itemId;
    private BigDecimal qtyRequired;
    private BigDecimal qtyIssued;
    private BigDecimal qtyConsumed;
    private BigDecimal qtyScrapped;
}
