package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.dto.StockMovementRequest;
import com.github.rrin.inventory.dto.StockMovementResponse;
import com.github.rrin.inventory.dto.StockReversalRequest;
import com.github.rrin.inventory.dto.filter.StockMovementFilter;
import com.github.rrin.inventory.service.StockMovementService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** The ledger is append-only, so there is deliberately no PUT or DELETE here. */
@RestController
@RequestMapping("/api/stock-movement")
public class StockMovementController {

    private final StockMovementService movementService;
    private final InventoryMapper mapper;

    @Autowired
    public StockMovementController(StockMovementService movementService, InventoryMapper mapper) {
        this.movementService = movementService;
        this.mapper = mapper;
    }

    @GetMapping
    public PaginatedResponse<StockMovementResponse> search(@ModelAttribute StockMovementFilter filter,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(movementService.search(filter, page, size).map(mapper::toResponse));
    }

    @GetMapping("/{id}")
    public StockMovementResponse get(@PathVariable UUID id) {
        return mapper.toResponse(movementService.get(id));
    }

    @PostMapping
    public StockMovementResponse post(@RequestBody StockMovementRequest request) {
        return mapper.toResponse(movementService.post(request));
    }

    @PostMapping("/{id}/reverse")
    public StockMovementResponse reverse(@PathVariable UUID id, @RequestBody StockReversalRequest request) {
        return mapper.toResponse(movementService.reverse(id, request));
    }
}
