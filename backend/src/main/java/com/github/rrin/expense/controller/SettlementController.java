package com.github.rrin.expense.controller;

import com.github.rrin.expense.dto.SettlementRequest;
import com.github.rrin.expense.dto.SettlementResponse;
import com.github.rrin.expense.service.SettlementResult;
import com.github.rrin.expense.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;
    private final FundingMapper fundingMapper;

    @Autowired
    public SettlementController(SettlementService settlementService, FundingMapper fundingMapper) {
        this.settlementService = settlementService;
        this.fundingMapper = fundingMapper;
    }

    // the user picks receipts and says how much cash they hand over for them
    @PostMapping
    public SettlementResponse settle(@RequestBody SettlementRequest request) {
        return toResponse(settlementService.settle(request));
    }

    private SettlementResponse toResponse(SettlementResult result) {
        return SettlementResponse.builder()
                .fundings(fundingMapper.toResponse(result.fundingsCreated()))
                .fromAdvances(result.fromAdvances())
                .fromNewCash(result.fromNewCash())
                .newDisbursementId(result.newDisbursement() != null ? result.newDisbursement().getId() : null)
                .leftOnNewAdvance(result.leftOnNewAdvance())
                .stillOwed(result.stillOwed())
                .build();
    }
}
