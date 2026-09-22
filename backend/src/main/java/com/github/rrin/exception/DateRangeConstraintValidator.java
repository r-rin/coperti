package com.github.rrin.exception;

import java.time.LocalDate;

public class DateRangeConstraintValidator extends ValidationCheck {
    public DateRangeConstraintValidator(LocalDate startDate, LocalDate endDate) {
        check(!startDate.isBefore(endDate), "Start date must be before end date");
    }
}
