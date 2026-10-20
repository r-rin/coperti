package com.github.rrin.production.dto;

import com.github.rrin.production.WorkOrderKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderRequest {
    private String code;
    private UUID productionOrderId;
    private UUID processId;
    private UUID itemId;
    private BigDecimal quantity;
    private WorkOrderKind kind;
    private UUID primaryAssigneeId;
    // process component id -> concrete item, one entry per BOM line that targets a category
    private Map<UUID, UUID> substitutes;
}
