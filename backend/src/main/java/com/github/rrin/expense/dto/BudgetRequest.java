package com.github.rrin.expense.dto;

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
public class BudgetRequest {
    private UUID id;
    private BigDecimal amount;
    private String givenBy;
    private String description;
    private LocalDate date;
}
