package com.github.rrin.expense.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseFilter {
    LocalDate fromDate;
    LocalDate toDate;
    String search;
    UUID issuedByEmployee;
}
