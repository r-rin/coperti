package com.github.rrin.expense.controller;

import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.FundingResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FundingMapper {

    public FundingResponse toResponse(Funding funding) {
        return FundingResponse.builder()
                .id(funding.getId())
                .disbursementId(funding.getDisbursement() != null ? funding.getDisbursement().getId() : null)
                .expenseId(funding.getExpense() != null ? funding.getExpense().getId() : null)
                .amountCovered(funding.getAmountCovered())
                .build();
    }

    public List<FundingResponse> toResponse(List<Funding> fundings) {
        return fundings.stream().map(this::toResponse).toList();
    }
}
