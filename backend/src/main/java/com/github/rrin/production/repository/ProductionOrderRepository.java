package com.github.rrin.production.repository;

import com.github.rrin.production.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, UUID>, JpaSpecificationExecutor<ProductionOrder> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
}
