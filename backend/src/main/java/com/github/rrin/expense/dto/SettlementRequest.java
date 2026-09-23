package com.github.rrin.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementRequest {
    private UUID employeeId;
    /** New cash handed over now; 0 settles from the employee's unspent advances alone. */
    private BigDecimal amount;
    /** Date of the new cash hand-out; today when absent. */
    private LocalDate date;
    private List<UUID> expenseIds;
}
