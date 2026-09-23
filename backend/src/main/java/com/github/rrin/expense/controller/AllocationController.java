package com.github.rrin.expense.controller;

import com.github.rrin.expense.dto.AllocationRequest;
import com.github.rrin.expense.dto.AllocationResponse;
import com.github.rrin.expense.service.AllocationResult;
import com.github.rrin.expense.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/allocation")
public class AllocationController {

    private final AllocationService allocationService;
    private final FundingMapper fundingMapper;

    @Autowired
    public AllocationController(AllocationService allocationService, FundingMapper fundingMapper) {
        this.allocationService = allocationService;
        this.fundingMapper = fundingMapper;
    }

    // the user picks which expenses this disbursement paid for
    @PostMapping
    public AllocationResponse allocate(@RequestBody AllocationRequest request) {
        return toResponse(allocationService.allocateManual(request));
    }

    private AllocationResponse toResponse(AllocationResult result) {
        return AllocationResponse.builder()
                .fundings(fundingMapper.toResponse(result.fundingsCreated()))
                .remainingBalance(result.remainingBalance())
                .disbursementStatus(result.disbursementStatus())
                .build();
    }
}
