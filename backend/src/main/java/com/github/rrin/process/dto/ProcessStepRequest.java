package com.github.rrin.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProcessStepRequest {
    private UUID id;
    private int seq;
    private UUID operationId;
    private UUID outputItemId;
    private int outputQuantity;
    private List<ProcessComponentRequest> components;
}
