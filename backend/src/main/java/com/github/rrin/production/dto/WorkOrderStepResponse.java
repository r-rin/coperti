package com.github.rrin.production.dto;

import com.github.rrin.production.WorkOrderStepStatus;
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
public class WorkOrderStepResponse {
    private UUID id;
    private UUID workOrderId;
    private UUID parentStepId;
    private int seq;
    private UUID operationId;
    private UUID outputItemId;
    private BigDecimal outputQuantity;
    private UUID assigneeId;
    private Instant plannedStart;
    private Instant deadline;
    private WorkOrderStepStatus status;
    private Instant startedAt;
    private Instant completedAt;
}
