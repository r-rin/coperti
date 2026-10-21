package com.github.rrin.inventory.repository.specs;

import com.github.rrin.inventory.OtherUse;
import com.github.rrin.inventory.OtherUseStatus;
import com.github.rrin.inventory.dto.filter.OtherUseFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class OtherUseSpecs {

    public static Specification<OtherUse> status(OtherUseStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<OtherUse> requestedBy(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("requestedBy").get("id"), employeeId);
        };
    }

    public static Specification<OtherUse> from(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("requestedAt"), from);
        };
    }

    public static Specification<OtherUse> to(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("requestedAt"), to);
        };
    }

    public static Specification<OtherUse> matching(OtherUseFilter filter) {
        return Specification.allOf(
                status(filter.getStatus()),
                requestedBy(filter.getRequestedById()),
                from(filter.getFrom()),
                to(filter.getTo())
        );
    }
}
