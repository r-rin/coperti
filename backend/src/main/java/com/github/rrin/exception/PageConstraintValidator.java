package com.github.rrin.exception;

public class PageConstraintValidator extends ValidationCheck {

    public static final int MAX_PAGE_SIZE = 100;

    public PageConstraintValidator(int page, int size) {
        check(page >= 0, "Page number must be greater than or equal to 0");
        check(size > 0 && size <= MAX_PAGE_SIZE, "Page size must be between 1 and " + MAX_PAGE_SIZE);
    }
}
