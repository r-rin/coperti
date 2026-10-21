package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.dto.MaterialMovementRequest;
import com.github.rrin.inventory.dto.StockMovementResponse;
import com.github.rrin.inventory.service.WorkOrderMaterialService;
import com.github.rrin.production.controller.ProductionMapper;
import com.github.rrin.production.dto.WorkOrderComponentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/work-order-material")
public class WorkOrderMaterialController {

    private final WorkOrderMaterialService materialService;
    private final InventoryMapper mapper;
    private final ProductionMapper productionMapper;

    @Autowired
    public WorkOrderMaterialController(WorkOrderMaterialService materialService,
                                       InventoryMapper mapper,
                                       ProductionMapper productionMapper) {
        this.materialService = materialService;
        this.mapper = mapper;
        this.productionMapper = productionMapper;
    }

    @PostMapping("/component/{id}/issue")
    public WorkOrderComponentResponse issue(@PathVariable UUID id, @RequestBody MaterialMovementRequest request) {
        return productionMapper.toResponse(materialService.issue(id, request));
    }

    @PostMapping("/component/{id}/consume")
    public WorkOrderComponentResponse consume(@PathVariable UUID id, @RequestBody MaterialMovementRequest request) {
        return productionMapper.toResponse(materialService.consume(id, request));
    }

    @PostMapping("/component/{id}/scrap")
    public WorkOrderComponentResponse scrap(@PathVariable UUID id, @RequestBody MaterialMovementRequest request) {
        return productionMapper.toResponse(materialService.scrap(id, request));
    }

    @PostMapping("/component/{id}/return")
    public WorkOrderComponentResponse returnUnused(@PathVariable UUID id, @RequestBody MaterialMovementRequest request) {
        return productionMapper.toResponse(materialService.returnUnused(id, request));
    }

    @PostMapping("/step/{id}/produce")
    public StockMovementResponse produce(@PathVariable UUID id, @RequestBody MaterialMovementRequest request) {
        return mapper.toResponse(materialService.produce(id, request));
    }
}
