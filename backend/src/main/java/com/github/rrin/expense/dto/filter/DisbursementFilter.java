package com.github.rrin.expense.dto.filter;

import com.github.rrin.expense.DisbursementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementFilter {
    private UUID givenToEmployee;
    private LocalDate fromDate;
    private LocalDate toDate;
    private DisbursementStatus status;
}
