package com.github.rrin.process.service;

import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessStepRequest;

import java.util.UUID;

public interface ProcessStepService {
    ProcessStep create(UUID processId, ProcessStepRequest request);
    ProcessStep update(ProcessStepRequest request);
    ProcessStep get(UUID id);
    ProcessStep delete(UUID id);
}
