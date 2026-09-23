package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.ReimbursementStatus;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.ReimbursementRequest;
import com.github.rrin.expense.dto.filter.ReimbursementFilter;
import com.github.rrin.expense.repository.ReimbursementRepository;
import com.github.rrin.expense.repository.specs.ReimbursementSpecs;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.ReimbursementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementRepository reimbursementRepository;
    private final ExpenseService expenseService;
    private final DisbursementService disbursementService;

    @Autowired
    public ReimbursementServiceImpl(
            ReimbursementRepository reimbursementRepository,
            ExpenseService expenseService,
            DisbursementService disbursementService
    ) {
        this.reimbursementRepository = reimbursementRepository;
        this.expenseService = expenseService;
        this.disbursementService = disbursementService;
    }

    @Override
    public Reimbursement create(ReimbursementRequest req) {
        Reimbursement rem = Reimbursement.builder()
                .amount(req.getAmount())
                .expense(expenseService.get(req.getExpenseId()))
                .build();

        return reimbursementRepository.save(rem);
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

    @Override
    public Reimbursement markAsPaid(UUID reimbursementId) {
        Reimbursement rem = getIfExists(reimbursementId);
        if (rem.getStatus() == ReimbursementStatus.PAID) {
            //TODO: separate exception for ForbiddenAction
            throw new IllegalStateException("Reimbursement is already marked as PAID");
        }

        DisbursementRequest disbursementRequest = DisbursementRequest.builder()
                .amount(rem.getAmount())
                .date(LocalDate.now())
                .employeeId(rem.getExpense().getEmployee().getId())
                .status(DisbursementStatus.CLOSED)
                .build();
        Disbursement payout = disbursementService.create(disbursementRequest);
        rem.setPayoutDisbursement(payout);
        rem.setStatus(ReimbursementStatus.PAID);
        //TODO: add funding entity creation
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
