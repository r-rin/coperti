package com.github.rrin.production.service;

import com.github.rrin.production.WorkOrderComponent;

import java.util.List;
import java.util.UUID;

public interface WorkOrderComponentService {
    WorkOrderComponent get(UUID id);
    List<WorkOrderComponent> getAllForWorkOrder(UUID workOrderId);
    List<WorkOrderComponent> getAllForStep(UUID stepId);
}
