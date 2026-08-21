package com.github.rrin.process.controller;

import com.github.rrin.process.dto.OperationRequest;
import com.github.rrin.process.dto.OperationResponse;
import com.github.rrin.process.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/operation")
public class OperationController {

    private final OperationService operationService;
    private final ProcessMapper mapper;

    @Autowired
    public OperationController(OperationService operationService, ProcessMapper mapper) {
        this.operationService = operationService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public OperationResponse get(@PathVariable UUID id) {
        return mapper.toResponse(operationService.get(id));
    }

    @PostMapping
    public OperationResponse create(@RequestBody OperationRequest request) {
        return mapper.toResponse(operationService.create(request));
    }

    @PutMapping
    public OperationResponse update(@RequestBody OperationRequest request) {
        return mapper.toResponse(operationService.update(request));
    }

    @DeleteMapping("/{id}")
    public OperationResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(operationService.delete(id));
    }
}
