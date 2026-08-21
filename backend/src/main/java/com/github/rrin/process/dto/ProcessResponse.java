package com.github.rrin.process.dto;

import com.github.rrin.process.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {
    private UUID id;
    private UUID producedItemId;
    private int version;
    private ProcessStatus status;
    private List<ProcessStepResponse> steps;
}
