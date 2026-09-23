package com.github.rrin.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementResponse {
    private List<FundingResponse> fundings;
    private BigDecimal fromAdvances;
    private BigDecimal fromNewCash;
    /** Null when no new cash was handed over. */
    private UUID newDisbursementId;
    private BigDecimal leftOnNewAdvance;
    private BigDecimal stillOwed;
}
