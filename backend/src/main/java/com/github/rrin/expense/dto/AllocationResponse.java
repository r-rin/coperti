package com.github.rrin.expense.dto;

import com.github.rrin.expense.DisbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllocationResponse {
    private List<FundingResponse> fundings;
    private BigDecimal remainingBalance;
    private DisbursementStatus disbursementStatus;
}
