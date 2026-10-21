package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.dto.ShortageResponse;
import com.github.rrin.inventory.dto.StockReservationRequest;
import com.github.rrin.inventory.dto.StockReservationResponse;
import com.github.rrin.inventory.service.StockReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-reservation")
public class StockReservationController {

    private final StockReservationService reservationService;
    private final InventoryMapper mapper;

    @Autowired
    public StockReservationController(StockReservationService reservationService, InventoryMapper mapper) {
        this.reservationService = reservationService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<StockReservationResponse> forWorkOrder(@RequestParam UUID workOrderId) {
        return reservationService.getForWorkOrder(workOrderId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public StockReservationResponse reserve(@RequestBody StockReservationRequest request) {
        return mapper.toResponse(reservationService.reserve(request));
    }

    @PostMapping("/work-order/{workOrderId}")
    public List<StockReservationResponse> reserveAll(@PathVariable UUID workOrderId) {
        return reservationService.reserveAll(workOrderId).stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/{id}/release")
    public StockReservationResponse release(@PathVariable UUID id) {
        return mapper.toResponse(reservationService.release(id));
    }

    @PostMapping("/work-order/{workOrderId}/release")
    public List<StockReservationResponse> releaseAll(@PathVariable UUID workOrderId) {
        return reservationService.releaseAll(workOrderId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/work-order/{workOrderId}/shortage")
    public List<ShortageResponse> shortage(@PathVariable UUID workOrderId) {
        return reservationService.shortage(workOrderId).stream().map(mapper::toResponse).toList();
    }
}
