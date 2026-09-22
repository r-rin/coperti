package com.github.rrin.expense.dto;

import com.github.rrin.expense.DisbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementResponse {
    private UUID id;
    private UUID employeeId;
    private BigDecimal amount;
    private LocalDate date;
    private DisbursementStatus status;
}
