package com.github.rrin.expense.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetFilter {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String givenBy;
    private String search;
}
