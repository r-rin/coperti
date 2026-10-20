package com.github.rrin.production.repository.specs;

import com.github.rrin.production.ProductionOrder;
import com.github.rrin.production.ProductionOrderStatus;
import com.github.rrin.production.dto.filter.ProductionOrderFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class ProductionOrderSpecs {

    public static Specification<ProductionOrder> product(UUID productId) {
        return (root, query, cb) -> {
            if (productId == null) return null;
            return cb.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<ProductionOrder> statusIn(Set<ProductionOrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(statuses);
        };
    }

    public static Specification<ProductionOrder> dueFrom(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("dueDate"), fromDate);
        };
    }

    public static Specification<ProductionOrder> dueTo(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("dueDate"), toDate);
        };
    }

    public static Specification<ProductionOrder> likeCodeOrDescription(String text) {
        return (root, query, cb) -> {
            if (text == null) return null;
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<ProductionOrder> matching(ProductionOrderFilter filter) {
        return Specification.allOf(
                product(filter.getProductId()),
                statusIn(filter.getStatuses()),
                dueFrom(filter.getFromDate()),
                dueTo(filter.getToDate()),
                likeCodeOrDescription(filter.getSearch())
        );
    }
}
