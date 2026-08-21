package com.github.rrin.process.dto;

import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProcessRequest {
    private UUID id;
    private UUID producedItemId;
    private int version;
    private ProcessStatus status;
    List<ProcessStep> steps;
}
