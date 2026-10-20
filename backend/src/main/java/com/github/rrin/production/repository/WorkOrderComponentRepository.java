package com.github.rrin.production.repository;

import com.github.rrin.production.WorkOrderComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkOrderComponentRepository extends JpaRepository<WorkOrderComponent, UUID> {
    List<WorkOrderComponent> findAllByWorkOrder_Id(UUID workOrderId);
    List<WorkOrderComponent> findAllByStep_Id(UUID stepId);
}
