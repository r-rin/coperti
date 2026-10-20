package com.github.rrin.production.dto;

import com.github.rrin.production.WorkOrderStepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepStatusHistoryResponse {
    private UUID id;
    private UUID stepId;
    private WorkOrderStepStatus fromStatus;
    private WorkOrderStepStatus toStatus;
    private Instant changedAt;
    private UUID changedById;
}
