package com.github.rrin.process.controller;

import com.github.rrin.process.dto.ProcessComponentRequest;
import com.github.rrin.process.dto.ProcessComponentResponse;
import com.github.rrin.process.service.ProcessComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/process/step")
public class ProcessComponentController {

    private final ProcessComponentService componentService;
    private final ProcessMapper mapper;

    @Autowired
    public ProcessComponentController(ProcessComponentService componentService, ProcessMapper mapper) {
        this.componentService = componentService;
        this.mapper = mapper;
    }

    @PostMapping("/{stepId}/component")
    public ProcessComponentResponse create(@PathVariable UUID stepId, @RequestBody ProcessComponentRequest request) {
        return mapper.toResponse(componentService.create(stepId, request));
    }

    @GetMapping("/{stepId}/component")
    public List<ProcessComponentResponse> getAllForStep(@PathVariable UUID stepId) {
        return componentService.getAllForStep(stepId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{stepId}/component")
    public List<ProcessComponentResponse> deleteAllForStep(@PathVariable UUID stepId) {
        return componentService.deleteAllForStep(stepId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/component/{id}")
    public ProcessComponentResponse get(@PathVariable UUID id) {
        return mapper.toResponse(componentService.get(id));
    }

    @PutMapping("/component")
    public ProcessComponentResponse update(@RequestBody ProcessComponentRequest request) {
        return mapper.toResponse(componentService.update(request));
    }

    @DeleteMapping("/component/{id}")
    public ProcessComponentResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(componentService.delete(id));
    }
}
