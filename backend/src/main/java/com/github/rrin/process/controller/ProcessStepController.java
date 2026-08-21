package com.github.rrin.process.controller;

import com.github.rrin.process.dto.ProcessStepRequest;
import com.github.rrin.process.dto.ProcessStepResponse;
import com.github.rrin.process.service.ProcessStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/process")
public class ProcessStepController {

    private final ProcessStepService stepService;
    private final ProcessMapper mapper;

    @Autowired
    public ProcessStepController(ProcessStepService stepService, ProcessMapper mapper) {
        this.stepService = stepService;
        this.mapper = mapper;
    }

    @PostMapping("/{processId}/step")
    public ProcessStepResponse create(@PathVariable UUID processId, @RequestBody ProcessStepRequest request) {
        return mapper.toResponse(stepService.create(processId, request));
    }

    @GetMapping("/step/{id}")
    public ProcessStepResponse get(@PathVariable UUID id) {
        return mapper.toResponse(stepService.get(id));
    }

    @PutMapping("/step")
    public ProcessStepResponse update(@RequestBody ProcessStepRequest request) {
        return mapper.toResponse(stepService.update(request));
    }

    @DeleteMapping("/step/{id}")
    public ProcessStepResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(stepService.delete(id));
    }
}
