package com.github.rrin.production.dto.filter;

import com.github.rrin.production.WorkOrderKind;
import com.github.rrin.production.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderFilter {
    private UUID productionOrderId;
    private UUID itemId;
    private UUID primaryAssigneeId;
    private WorkOrderKind kind;
    private Set<WorkOrderStatus> statuses;
}
