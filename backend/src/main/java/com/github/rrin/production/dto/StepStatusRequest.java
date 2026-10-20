package com.github.rrin.production.dto;

import com.github.rrin.production.WorkOrderStepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepStatusRequest {
    private WorkOrderStepStatus status;
    private UUID changedById;
}
