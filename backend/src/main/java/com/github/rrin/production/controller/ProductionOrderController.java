package com.github.rrin.production.controller;

import com.github.rrin.production.dto.ProductionOrderRequest;
import com.github.rrin.production.dto.ProductionOrderResponse;
import com.github.rrin.production.dto.filter.ProductionOrderFilter;
import com.github.rrin.production.service.ProductionOrderService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production-order")
public class ProductionOrderController {

    private final ProductionOrderService orderService;
    private final ProductionMapper mapper;

    @Autowired
    public ProductionOrderController(ProductionOrderService orderService, ProductionMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ProductionOrderResponse get(@PathVariable UUID id) {
        return mapper.toResponse(orderService.get(id));
    }

    @GetMapping
    public PaginatedResponse<ProductionOrderResponse> search(@ModelAttribute ProductionOrderFilter filter,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(orderService.search(filter, page, size).map(mapper::toResponse));
    }

    @PostMapping
    public ProductionOrderResponse create(@RequestBody ProductionOrderRequest request) {
        return mapper.toResponse(orderService.create(request));
    }

    @PutMapping
    public ProductionOrderResponse update(@RequestBody ProductionOrderRequest request) {
        return mapper.toResponse(orderService.update(request));
    }

    @DeleteMapping("/{id}")
    public ProductionOrderResponse delete(@PathVariable UUID id) {
        return mapper.toResponse(orderService.delete(id));
    }

    @PostMapping("/{id}/release")
    public ProductionOrderResponse release(@PathVariable UUID id) {
        return mapper.toResponse(orderService.release(id));
    }

    @PostMapping("/{id}/complete")
    public ProductionOrderResponse complete(@PathVariable UUID id) {
        return mapper.toResponse(orderService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    public ProductionOrderResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(orderService.cancel(id));
    }
}
