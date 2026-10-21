package com.github.rrin.inventory.repository.specs;

import com.github.rrin.inventory.MovementType;
import com.github.rrin.inventory.StockMovement;
import com.github.rrin.inventory.dto.filter.StockMovementFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class StockMovementSpecs {

    public static Specification<StockMovement> item(UUID itemId) {
        return (root, query, cb) -> {
            if (itemId == null) return null;
            return cb.equal(root.get("item").get("id"), itemId);
        };
    }

    public static Specification<StockMovement> touchingLocation(UUID locationId) {
        return (root, query, cb) -> {
            if (locationId == null) return null;
            return cb.or(
                    cb.equal(root.get("fromLocation").get("id"), locationId),
                    cb.equal(root.get("toLocation").get("id"), locationId)
            );
        };
    }

    public static Specification<StockMovement> typeIn(Set<MovementType> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) return null;
            return root.get("movementType").in(types);
        };
    }

    public static Specification<StockMovement> workOrder(UUID workOrderId) {
        return (root, query, cb) -> {
            if (workOrderId == null) return null;
            return cb.equal(root.get("workOrder").get("id"), workOrderId);
        };
    }

    public static Specification<StockMovement> performedBy(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("performedBy").get("id"), employeeId);
        };
    }

    public static Specification<StockMovement> from(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
        };
    }

    public static Specification<StockMovement> to(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("occurredAt"), to);
        };
    }

    public static Specification<StockMovement> matching(StockMovementFilter filter) {
        return Specification.allOf(
                item(filter.getItemId()),
                touchingLocation(filter.getLocationId()),
                typeIn(filter.getTypes()),
                workOrder(filter.getWorkOrderId()),
                performedBy(filter.getPerformedById()),
                from(filter.getFrom()),
                to(filter.getTo())
        );
    }
}
