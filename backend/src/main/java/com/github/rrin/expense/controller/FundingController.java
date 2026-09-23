package com.github.rrin.expense.controller;

import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.dto.FundingResponse;
import com.github.rrin.expense.service.FundingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funding")
public class FundingController {

    private final FundingService fundingService;
    private final FundingMapper fundingMapper;

    @Autowired
    public FundingController(FundingService fundingService, FundingMapper fundingMapper) {
        this.fundingService = fundingService;
        this.fundingMapper = fundingMapper;
    }

    @PostMapping
    public FundingResponse create(@RequestBody FundingRequest request) {
        return fundingMapper.toResponse(fundingService.create(request));
    }

    @GetMapping("/expense/{expenseId}")
    public List<FundingResponse> getByExpense(@PathVariable UUID expenseId) {
        return fundingMapper.toResponse(fundingService.getByExpense(expenseId));
    }

    @GetMapping("/disbursement/{disbursementId}")
    public List<FundingResponse> getByDisbursement(@PathVariable UUID disbursementId) {
        return fundingMapper.toResponse(fundingService.getByDisbursement(disbursementId));
    }

    @GetMapping("/expense/{expenseId}/covered")
    public BigDecimal getCoveredAmount(@PathVariable UUID expenseId) {
        return fundingService.getCoveredAmount(expenseId);
    }

    @GetMapping("/expense/{expenseId}/shortfall")
    public BigDecimal getShortfall(@PathVariable UUID expenseId) {
        return fundingService.getShortfall(expenseId);
    }

    @GetMapping("/disbursement/{disbursementId}/remaining")
    public BigDecimal getRemainingBalance(@PathVariable UUID disbursementId) {
        return fundingService.getRemainingBalance(disbursementId);
    }
}
