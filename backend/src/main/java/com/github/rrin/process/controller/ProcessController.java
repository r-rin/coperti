package com.github.rrin.process.controller;

import com.github.rrin.process.dto.ProcessRequest;
import com.github.rrin.process.dto.ProcessResponse;
import com.github.rrin.process.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessService processService;
    private final ProcessMapper mapper;

    @Autowired
    public ProcessController(ProcessService processService, ProcessMapper mapper) {
        this.processService = processService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ProcessResponse get(@PathVariable UUID id) {
        return mapper.toResponse(processService.get(id));
    }

    @PostMapping
    public ProcessResponse create(@RequestBody ProcessRequest request) {
        return mapper.toResponse(processService.create(request));
    }

    @PutMapping
    public ProcessResponse update(@RequestBody ProcessRequest request) {
        return mapper.toResponse(processService.update(request));
    }

    @DeleteMapping("/{id}")
    public ProcessResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(processService.delete(id));
    }

    @PostMapping("/{id}/draft")
    public ProcessResponse draft(@PathVariable UUID id) {
        return mapper.toResponse(processService.setDrafted(id));
    }

    @PostMapping("/{id}/activate")
    public ProcessResponse activate(@PathVariable UUID id) {
        return mapper.toResponse(processService.setActive(id));
    }

    @PostMapping("/{id}/archive")
    public ProcessResponse archive(@PathVariable UUID id) {
        return mapper.toResponse(processService.setArchived(id));
    }
}
