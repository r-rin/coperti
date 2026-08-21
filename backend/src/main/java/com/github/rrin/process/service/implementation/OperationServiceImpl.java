package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.process.Operation;
import com.github.rrin.process.dto.OperationRequest;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.process.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;

    @Autowired
    public OperationServiceImpl(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public Operation create(OperationRequest request) {
        Operation operation = new Operation();
        operation.setCode(request.getCode());
        operation.setName(request.getName());
        operation.setDescription(request.getDescription());
        return operationRepository.save(operation);
    }

    @Override
    public Operation update(OperationRequest request) {
        Operation operation = checkIfOperationExists(request.getId());
        operation.setCode(request.getCode());
        operation.setName(request.getName());
        operation.setDescription(request.getDescription());
        return operationRepository.save(operation);
    }

    @Override
    public Operation get(UUID id) {
        return checkIfOperationExists(id);
    }

    @Override
    public Operation delete(UUID id) {
        Operation operation = checkIfOperationExists(id);
        operationRepository.delete(operation);
        return operation;
    }

    private Operation checkIfOperationExists(UUID id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Operation not found with id: " + id));
    }
}
