package com.github.rrin.expense.repository.specs;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.filter.ExpenseFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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

    // same rule as FundingService.getShortfall: uncovered while the funding sum is below the amount
    public static Specification<Expense> uncovered(Boolean uncoveredOnly){
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(uncoveredOnly)) return null;
            Subquery<BigDecimal> covered = query.subquery(BigDecimal.class);
            Root<Funding> funding = covered.from(Funding.class);
            covered.select(cb.coalesce(cb.sum(funding.get("amountCovered")), BigDecimal.ZERO))
                    .where(cb.equal(funding.get("expense"), root));
            return cb.lessThan(covered, root.<BigDecimal>get("amount"));
        };
    }

    public static Specification<Expense> matching(ExpenseFilter filter){
        return Specification.allOf(
                fromDate(filter.getFromDate()),
                toDate(filter.getToDate()),
                issuedBy(filter.getIssuedByEmployee()),
                likeDescription(filter.getSearch()),
                uncovered(filter.getUncoveredOnly())
        );
    }
}
