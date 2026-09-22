package com.github.rrin.expense.repository.specs;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.dto.filter.ExpenseFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ExpenseSpecs {

    public static Specification<Expense> fromDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("date"), startDate);
        };
    }


    public static Specification<Expense> toDate(LocalDate endDate){
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("date"), endDate);
        };
    }

    public static Specification<Expense> issuedBy(UUID employeeId){
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("employee").get("id"), employeeId);
        };
    }

    public static Specification<Expense> likeDescription(String text){
        return (root, query, cb) -> {
            if (text == null) return null;
            return cb.like(root.get("description"), "%" + text + "%");
        };
    }

    public static Specification<Expense> matching(ExpenseFilter filter){
        return Specification.allOf(
                fromDate(filter.getFromDate()),
                toDate(filter.getToDate()),
                issuedBy(filter.getIssuedByEmployee()),
                likeDescription(filter.getSearch())
        );
    }
}
