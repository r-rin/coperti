package com.github.rrin.production.repository.specs;

import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderKind;
import com.github.rrin.production.WorkOrderStatus;
import com.github.rrin.production.dto.filter.WorkOrderFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

public class WorkOrderSpecs {

    public static Specification<WorkOrder> productionOrder(UUID productionOrderId) {
        return (root, query, cb) -> {
            if (productionOrderId == null) return null;
            return cb.equal(root.get("order").get("id"), productionOrderId);
        };
    }

    public static Specification<WorkOrder> item(UUID itemId) {
        return (root, query, cb) -> {
            if (itemId == null) return null;
            return cb.equal(root.get("item").get("id"), itemId);
        };
    }

    public static Specification<WorkOrder> primaryAssignee(UUID employeeId) {
        return (root, query, cb) -> {
            if (employeeId == null) return null;
            return cb.equal(root.get("primaryAssignee").get("id"), employeeId);
        };
    }

    public static Specification<WorkOrder> kind(WorkOrderKind kind) {
        return (root, query, cb) -> {
            if (kind == null) return null;
            return cb.equal(root.get("kind"), kind);
        };
    }

    public static Specification<WorkOrder> statusIn(Set<WorkOrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(statuses);
        };
    }

    public static Specification<WorkOrder> matching(WorkOrderFilter filter) {
        return Specification.allOf(
                productionOrder(filter.getProductionOrderId()),
                item(filter.getItemId()),
                primaryAssignee(filter.getPrimaryAssigneeId()),
                kind(filter.getKind()),
                statusIn(filter.getStatuses())
        );
    }
}
