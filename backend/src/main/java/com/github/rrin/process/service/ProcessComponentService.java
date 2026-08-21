package com.github.rrin.process.service;

import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.dto.ProcessComponentRequest;

import java.util.List;
import java.util.UUID;

public interface ProcessComponentService {
    ProcessComponent create(UUID processStepId, ProcessComponentRequest request);
    ProcessComponent update(ProcessComponentRequest request);
    ProcessComponent get(UUID id);
    ProcessComponent delete(UUID id);
    List<ProcessComponent> getAllForStep(UUID processStepId);
    List<ProcessComponent> deleteAllForStep(UUID processStepId);
}
