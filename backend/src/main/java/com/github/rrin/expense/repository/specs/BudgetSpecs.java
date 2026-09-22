package com.github.rrin.expense.repository.specs;

import com.github.rrin.expense.Budget;
import com.github.rrin.expense.dto.filter.BudgetFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BudgetSpecs {

    public static Specification<Budget> fromDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;

            return cb.greaterThanOrEqualTo(root.get("date"), startDate);
        };
    }

    public static Specification<Budget> toDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;

            return cb.lessThanOrEqualTo(root.get("date"), endDate);
        };
    }

    public static Specification<Budget> likeDescription(String text) {
        return (root, query, cb) -> {
            if (text == null) return null;

            return cb.like(root.get("description"), "%" + text + "%");
        };
    }

    public static Specification<Budget> likeGivenBy(String text) {
        return (root, query, cb) -> {
            if (text == null) return null;

            return cb.like(root.get("givenBy"), "%" + text + "%");
        };
    }


    public static Specification<Budget> matching(BudgetFilter filter) {
        return Specification.allOf(
                fromDate(filter.getFromDate()),
                toDate(filter.getToDate()),
                likeDescription(filter.getSearch()),
                likeGivenBy(filter.getGivenBy())
        );
    }
}