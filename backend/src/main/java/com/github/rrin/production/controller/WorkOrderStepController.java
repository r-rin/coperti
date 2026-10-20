package com.github.rrin.production.controller;

import com.github.rrin.production.dto.*;
import com.github.rrin.production.service.StepHistoryService;
import com.github.rrin.production.service.WorkOrderComponentService;
import com.github.rrin.production.service.WorkOrderStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-order-step")
public class WorkOrderStepController {

    private final WorkOrderStepService stepService;
    private final WorkOrderComponentService componentService;
    private final StepHistoryService historyService;
    private final ProductionMapper mapper;

    @Autowired
    public WorkOrderStepController(WorkOrderStepService stepService,
                                   WorkOrderComponentService componentService,
                                   StepHistoryService historyService,
                                   ProductionMapper mapper) {
        this.stepService = stepService;
        this.componentService = componentService;
        this.historyService = historyService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public WorkOrderStepResponse get(@PathVariable UUID id) {
        return mapper.toResponse(stepService.get(id));
    }

    @PostMapping
    public WorkOrderStepResponse addSubtask(@RequestBody WorkOrderStepRequest request) {
        return mapper.toResponse(stepService.addSubtask(request));
    }

    @PutMapping
    public WorkOrderStepResponse reschedule(@RequestBody WorkOrderStepRequest request) {
        return mapper.toResponse(stepService.reschedule(request));
    }

    @PostMapping("/{id}/assign")
    public WorkOrderStepResponse assign(@PathVariable UUID id, @RequestBody StepAssignRequest request) {
        return mapper.toResponse(stepService.assign(id, request));
    }

    @PostMapping("/{id}/status")
    public WorkOrderStepResponse changeStatus(@PathVariable UUID id, @RequestBody StepStatusRequest request) {
        return mapper.toResponse(stepService.changeStatus(id, request));
    }

    @GetMapping("/{id}/subtasks")
    public List<WorkOrderStepResponse> subtasks(@PathVariable UUID id) {
        return stepService.getSubtasks(id).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}/components")
    public List<WorkOrderComponentResponse> components(@PathVariable UUID id) {
        return componentService.getAllForStep(id).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}/assignments")
    public List<StepAssignmentResponse> assignments(@PathVariable UUID id) {
        return historyService.getAssignmentsForStep(id).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}/status-history")
    public List<StepStatusHistoryResponse> statusHistory(@PathVariable UUID id) {
        return historyService.getStatusHistoryForStep(id).stream().map(mapper::toResponse).toList();
    }
}
