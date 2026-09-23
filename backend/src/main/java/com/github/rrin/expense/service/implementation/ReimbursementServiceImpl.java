package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.ReimbursementStatus;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.dto.ReimbursementRequest;
import com.github.rrin.expense.dto.filter.ReimbursementFilter;
import com.github.rrin.expense.repository.ReimbursementRepository;
import com.github.rrin.expense.repository.specs.ReimbursementSpecs;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.FundingService;
import com.github.rrin.expense.service.ReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementRepository reimbursementRepository;
    private final ExpenseService expenseService;
    private final DisbursementService disbursementService;
    private final FundingService fundingService;

    @Autowired
    public ReimbursementServiceImpl(
            ReimbursementRepository reimbursementRepository,
            ExpenseService expenseService,
            DisbursementService disbursementService,
            FundingService fundingService
    ) {
        this.reimbursementRepository = reimbursementRepository;
        this.expenseService = expenseService;
        this.disbursementService = disbursementService;
        this.fundingService = fundingService;
    }

    @Override
    public Reimbursement create(ReimbursementRequest req) {
        new ValidationCheck()
                .check(req.getExpenseId() != null, "Expense id is required")
                .check(req.getAmount() != null && req.getAmount().compareTo(BigDecimal.ZERO) > 0,
                        "Amount must be greater than 0")
                .throwIfAny(ValidationException::new);

        Expense expense = expenseService.get(req.getExpenseId());
        ReimbursementGuard.requireNotAlreadyRaised(expense, reimbursementRepository.existsByExpenseId(expense.getId()));

        Reimbursement rem = Reimbursement.builder()
                .amount(req.getAmount())
                .expense(expense)
                // @ColumnDefault only shapes the DDL; Hibernate still inserts an explicit null without this
                .status(ReimbursementStatus.PENDING)
                .build();

        return reimbursementRepository.save(rem);
    }

    @Override
    public Reimbursement createForShortfall(UUID expenseId) {
        Expense expense = expenseService.get(expenseId);
        BigDecimal shortfall = fundingService.getShortfall(expense.getId());

        ReimbursementGuard.requireShortfall(expense, shortfall);

        return create(ReimbursementRequest.builder()
                .expenseId(expense.getId())
                .amount(shortfall)
                .build());
    }

    @Override
    public Reimbursement update(ReimbursementRequest reimbursementRequest) {
        Reimbursement rem = getIfExists(reimbursementRequest.getId());
        rem.setAmount(reimbursementRequest.getAmount());
        return reimbursementRepository.save(rem);
    }

    @Override
    public Reimbursement get(UUID reimbursementId) {
        return getIfExists(reimbursementId);
    }

    @Override
    public Reimbursement delete(UUID reimbursementId) {
        Reimbursement rem = getIfExists(reimbursementId);
        reimbursementRepository.delete(rem);
        return rem;
    }

    /**
     * Paying a reimbursement is money leaving the facility, so it goes through the same door as every
     * other hand-out: a new Disbursement, immediately drawn down by a Funding row against the original
     * expense. That keeps one "money out" formula and leaves the expense showing fully funded.
     */
    @Override
    @Transactional
    public Reimbursement markAsPaid(UUID reimbursementId) {
        Reimbursement rem = getIfExists(reimbursementId);
        ReimbursementGuard.requirePayable(rem);

        // status is not set here: create() always opens a disbursement, and the funding row below
        // drains it to zero, which closes it through the guarded transition
        Disbursement payout = disbursementService.create(DisbursementRequest.builder()
                .amount(rem.getAmount())
                .date(LocalDate.now())
                .employeeId(rem.getExpense().getEmployee().getId())
                .build());

        fundingService.create(FundingRequest.builder()
                .disbursementId(payout.getId())
                .expenseId(rem.getExpense().getId())
                .amountCovered(rem.getAmount())
                .build());

        rem.setPayoutDisbursement(payout);
        rem.setStatus(ReimbursementStatus.PAID);
        return reimbursementRepository.save(rem);
    }

    @Override
    public BigDecimal sum(ReimbursementFilter filter) {
        return reimbursementRepository.sumAmount(ReimbursementSpecs.matching(filter));
    }

    @Override
    public Page<Reimbursement> search(ReimbursementFilter filter, int page, int size) {
        new PageConstraintValidator(page, size)
                .throwIfAny(InvalidQuery::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expense.date"));
        return reimbursementRepository.findAll(ReimbursementSpecs.matching(filter), pageable);
    }

    private Reimbursement getIfExists(UUID id) {
        return reimbursementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Reimbursement with id " + id + " not found")
        );
    }
}
