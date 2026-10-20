package com.github.rrin.production.service;

import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.dto.WorkOrderRequest;
import com.github.rrin.production.dto.filter.WorkOrderFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface WorkOrderService {
    WorkOrder get(UUID id);
    Page<WorkOrder> search(WorkOrderFilter filter, int page, int size);

    /** Freezes the process into steps and components; a released work order is never edited, only progressed. */
    WorkOrder release(WorkOrderRequest request);
    WorkOrder complete(UUID id);
    WorkOrder cancel(UUID id, UUID changedById);

    /** Called when one of the work order's steps starts; a RELEASED work order becomes IN_PROGRESS. */
    WorkOrder markInProgress(UUID id);
}
