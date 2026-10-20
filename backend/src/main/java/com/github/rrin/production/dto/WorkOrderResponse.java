package com.github.rrin.production.dto;

import com.github.rrin.production.WorkOrderKind;
import com.github.rrin.production.WorkOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponse {
    private UUID id;
    private String code;
    private UUID productionOrderId;
    private UUID itemId;
    private UUID processId;
    private BigDecimal quantity;
    private WorkOrderKind kind;
    private UUID primaryAssigneeId;
    private WorkOrderStatus status;
    private Instant releasedAt;
    private Instant completedAt;
}
