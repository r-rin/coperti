package com.github.rrin.production.repository;

import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID>, JpaSpecificationExecutor<WorkOrder> {
    boolean existsByCode(String code);
    boolean existsByOrder_Id(UUID productionOrderId);
    boolean existsByOrder_IdAndStatusIn(UUID productionOrderId, Collection<WorkOrderStatus> statuses);
    List<WorkOrder> findAllByOrder_Id(UUID productionOrderId);
}
