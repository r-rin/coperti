package com.github.rrin.exception;

import java.time.LocalDate;

public class DateRangeConstraintValidator extends ValidationCheck {
    public DateRangeConstraintValidator(LocalDate startDate, LocalDate endDate) {
        check(startDate == null || endDate == null || !startDate.isAfter(endDate),
                "Start date must not be after end date");
    }
}
