package com.github.rrin.production.repository;

import com.github.rrin.production.WorkOrderStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkOrderStepRepository extends JpaRepository<WorkOrderStep, UUID> {
    List<WorkOrderStep> findAllByWorkOrder_IdOrderBySeqAsc(UUID workOrderId);
    List<WorkOrderStep> findAllByWorkOrder_IdAndParentIsNullOrderBySeqAsc(UUID workOrderId);
    List<WorkOrderStep> findAllByParent_IdOrderBySeqAsc(UUID parentStepId);
}
