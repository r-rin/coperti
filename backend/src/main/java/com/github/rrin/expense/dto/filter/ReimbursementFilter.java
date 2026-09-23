package com.github.rrin.expense.dto.filter;

import com.github.rrin.expense.ReimbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementFilter {
    UUID expenseId;
    ReimbursementStatus status;
    UUID employeeId;
}
