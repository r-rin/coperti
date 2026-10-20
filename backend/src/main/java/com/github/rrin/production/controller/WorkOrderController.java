package com.github.rrin.production.controller;

import com.github.rrin.production.dto.WorkOrderComponentResponse;
import com.github.rrin.production.dto.WorkOrderRequest;
import com.github.rrin.production.dto.WorkOrderResponse;
import com.github.rrin.production.dto.WorkOrderStepResponse;
import com.github.rrin.production.dto.filter.WorkOrderFilter;
import com.github.rrin.production.service.WorkOrderComponentService;
import com.github.rrin.production.service.WorkOrderService;
import com.github.rrin.production.service.WorkOrderStepService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderStepService stepService;
    private final WorkOrderComponentService componentService;
    private final ProductionMapper mapper;

    @Autowired
    public WorkOrderController(WorkOrderService workOrderService,
                               WorkOrderStepService stepService,
                               WorkOrderComponentService componentService,
                               ProductionMapper mapper) {
        this.workOrderService = workOrderService;
        this.stepService = stepService;
        this.componentService = componentService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public WorkOrderResponse get(@PathVariable UUID id) {
        return mapper.toResponse(workOrderService.get(id));
    }

    @GetMapping
    public PaginatedResponse<WorkOrderResponse> search(@ModelAttribute WorkOrderFilter filter,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(workOrderService.search(filter, page, size).map(mapper::toResponse));
    }

    @PostMapping
    public WorkOrderResponse release(@RequestBody WorkOrderRequest request) {
        return mapper.toResponse(workOrderService.release(request));
    }

    @PostMapping("/{id}/complete")
    public WorkOrderResponse complete(@PathVariable UUID id) {
        return mapper.toResponse(workOrderService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    public WorkOrderResponse cancel(@PathVariable UUID id, @RequestParam(required = false) UUID changedById) {
        return mapper.toResponse(workOrderService.cancel(id, changedById));
    }

    @GetMapping("/{id}/steps")
    public List<WorkOrderStepResponse> steps(@PathVariable UUID id) {
        return stepService.getAllForWorkOrder(id).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}/components")
    public List<WorkOrderComponentResponse> components(@PathVariable UUID id) {
        return componentService.getAllForWorkOrder(id).stream().map(mapper::toResponse).toList();
    }
}
