package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.dto.StockBalanceResponse;
import com.github.rrin.inventory.service.StockBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-balance")
public class StockBalanceController {

    private final StockBalanceService balanceService;
    private final InventoryMapper mapper;

    @Autowired
    public StockBalanceController(StockBalanceService balanceService, InventoryMapper mapper) {
        this.balanceService = balanceService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<StockBalanceResponse> find(@RequestParam(required = false) UUID itemId,
                                           @RequestParam(required = false) UUID locationId,
                                           @RequestParam(required = false) UUID employeeId) {
        return balanceService.find(itemId, locationId, employeeId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/available/{itemId}")
    public BigDecimal available(@PathVariable UUID itemId) {
        return balanceService.available(itemId);
    }

    @PostMapping("/rebuild")
    public void rebuild() {
        balanceService.rebuild();
    }
}
