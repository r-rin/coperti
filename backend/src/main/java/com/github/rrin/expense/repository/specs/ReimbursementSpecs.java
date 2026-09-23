package com.github.rrin.expense.repository.specs;

import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.ReimbursementStatus;
import com.github.rrin.expense.dto.filter.ReimbursementFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ReimbursementSpecs {

    public static Specification<Reimbursement> byExpenseId(UUID expenseId) {
        return (root, query, cb) -> {
            if (expenseId == null) return null;
            return cb.equal(root.get("expense").get("id"), expenseId);
        };
    }

    // reached through expense, so the FK on EXPENSE is enough — EMPLOYEE is never joined
    public static Specification<Reimbursement> byEmployeeId(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("expense").get("employee").get("id"), employeeId);
        };
    }

    public static Specification<Reimbursement> byStatus(ReimbursementStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Reimbursement> matching(ReimbursementFilter filter) {
        return Specification.allOf(
                byExpenseId(filter.getExpenseId()),
                byEmployeeId(filter.getEmployeeId()),
                byStatus(filter.getStatus())
        );
    }
}
