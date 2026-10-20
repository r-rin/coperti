package com.github.rrin.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderStepRequest {
    private UUID id;
    private UUID parentStepId;
    private int seq;
    private UUID operationId;
    private UUID outputItemId;
    private BigDecimal outputQuantity;
    private Instant plannedStart;
    private Instant deadline;
    private List<WorkOrderComponentRequest> components;
}
