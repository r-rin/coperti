package com.github.rrin.expense.repository.specs;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class DisbursementSpecs {

    public static Specification<Disbursement> fromDate(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("date"), startDate);
        };
    }

    public static Specification<Disbursement> toDate(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("date"), endDate);
        };
    }

    public static Specification<Disbursement> givenToEmployee(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("employee").get("id"), employeeId);
        };
    }

    public static Specification<Disbursement> statusIs(DisbursementStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Disbursement> matching(DisbursementFilter filter) {
        return Specification.allOf(
                fromDate(filter.getFromDate()),
                toDate(filter.getToDate()),
                givenToEmployee(filter.getGivenToEmployee()),
                statusIs(filter.getStatus())
        );
    }
}
