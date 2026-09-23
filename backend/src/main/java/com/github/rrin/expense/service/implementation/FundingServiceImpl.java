package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.repository.FundingRepository;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.FundingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FundingServiceImpl implements FundingService {

    private final FundingRepository fundingRepository;
    private final DisbursementService disbursementService;
    private final ExpenseService expenseService;

    @Autowired
    public FundingServiceImpl(FundingRepository fundingRepository,
                              DisbursementService disbursementService,
                              ExpenseService expenseService) {
        this.fundingRepository = fundingRepository;
        this.disbursementService = disbursementService;
        this.expenseService = expenseService;
    }

    @Override
    @Transactional
    public Funding create(FundingRequest request) {
        new ValidationCheck()
                .check(request.getDisbursementId() != null, "Disbursement id is required")
                .check(request.getExpenseId() != null, "Expense id is required")
                .check(request.getAmountCovered() != null
                        && request.getAmountCovered().compareTo(BigDecimal.ZERO) > 0,
                        "Amount covered must be greater than 0")
                .throwIfAny(ValidationException::new);

        Disbursement disbursement = disbursementService.getById(request.getDisbursementId());
        Expense expense = expenseService.get(request.getExpenseId());

        BigDecimal remaining = getRemainingBalance(disbursement.getId());
        BigDecimal shortfall = getShortfall(expense.getId());

        FundingGuard.requireDrawable(disbursement, expense, request.getAmountCovered(), remaining, shortfall);

        Funding funding = Funding.builder()
                .disbursement(disbursement)
                .expense(expense)
                .amountCovered(request.getAmountCovered())
                .build();
        // flushed so the balance recomputed below sees this row without relying on the session flush mode
        Funding saved = fundingRepository.saveAndFlush(funding);

        closeIfDrained(disbursement);
        return saved;
    }

    @Override
    public List<Funding> getByExpense(UUID expenseId) {
        return fundingRepository.findByExpenseId(expenseService.get(expenseId).getId());
    }

    @Override
    public List<Funding> getByDisbursement(UUID disbursementId) {
        return fundingRepository.findByDisbursementId(disbursementService.getById(disbursementId).getId());
    }

    @Override
    public BigDecimal getCoveredAmount(UUID expenseId) {
        return fundingRepository.sumAmountCoveredByExpenseId(expenseService.get(expenseId).getId());
    }

    @Override
    public BigDecimal getShortfall(UUID expenseId) {
        Expense expense = expenseService.get(expenseId);
        BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;
        BigDecimal covered = fundingRepository.sumAmountCoveredByExpenseId(expense.getId());
        return amount.subtract(covered).max(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getRemainingBalance(UUID disbursementId) {
        Disbursement disbursement = disbursementService.getById(disbursementId);
        BigDecimal amount = disbursement.getAmount() != null ? disbursement.getAmount() : BigDecimal.ZERO;
        BigDecimal drawn = fundingRepository.sumAmountCoveredByDisbursementId(disbursement.getId());
        return amount.subtract(drawn).max(BigDecimal.ZERO);
    }

    /**
     * An advance with nothing left is settled by definition, so CLOSED is maintained by the ledger
     * rather than set by hand. It still goes through the guarded transition like every other caller.
     */
    private void closeIfDrained(Disbursement disbursement) {
        if (getRemainingBalance(disbursement.getId()).compareTo(BigDecimal.ZERO) == 0) {
            disbursementService.updateStatus(disbursement.getId(), DisbursementStatus.CLOSED);
        }
    }
}
