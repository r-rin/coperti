package com.github.rrin.process.service;

import com.github.rrin.process.Operation;
import com.github.rrin.process.dto.OperationRequest;

import java.util.UUID;

public interface OperationService {
    Operation create(OperationRequest request);
    Operation update(OperationRequest request);
    Operation get(UUID id);
    Operation delete(UUID id);
}
