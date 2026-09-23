package com.github.rrin.expense.dto;

import com.github.rrin.expense.ReimbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementResponse {
    private UUID id;
    private UUID expenseId;
    private UUID employeeId;
    private BigDecimal amount;
    private ReimbursementStatus status;
    private UUID payoutDisbursementId;
}
