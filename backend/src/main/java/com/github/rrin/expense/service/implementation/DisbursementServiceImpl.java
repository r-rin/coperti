package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.DateRangeConstraintValidator;
import com.github.rrin.exception.PageConstraintValidator;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import com.github.rrin.expense.repository.DisbursementRepository;
import com.github.rrin.expense.repository.specs.DisbursementSpecs;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DisbursementServiceImpl implements DisbursementService {

    private DisbursementRepository disbursementRepository;
    private EmployeeRepository employeeRepository;

    @Autowired
    public DisbursementServiceImpl(DisbursementRepository disbursementRepository, EmployeeRepository employeeRepository) {
        this.disbursementRepository = disbursementRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Disbursement create(DisbursementRequest request) {
        Disbursement disbursement = Disbursement.builder()
                .employee(getEmployee(request.getEmployeeId()))
                .amount(request.getAmount())
                .date(request.getDate())
                .status(request.getStatus())
                .build();
        return disbursementRepository.save(disbursement);
    }

    @Override
    public Disbursement updateStatus(UUID id, DisbursementStatus status) {
        Disbursement disbursement = getIfExists(id);
        disbursement.setStatus(status);
        return disbursementRepository.save(disbursement);
    }

    @Override
    public Disbursement getById(UUID id) {
        return getIfExists(id);
    }

    @Override
    public Page<Disbursement> search(DisbursementFilter filter, int page, int size) {
        new PageConstraintValidator(page,size).throwIfAny(InvalidQuery::new);
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate()).throwIfAny(InvalidQuery::new);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return disbursementRepository.findAll(DisbursementSpecs.matching(filter), pageable);
    }

    @Override
    public BigDecimal sum(DisbursementFilter filter) {
        new DateRangeConstraintValidator(filter.getFromDate(), filter.getToDate()).throwIfAny(InvalidQuery::new);

        return disbursementRepository.sumAmount(DisbursementSpecs.matching(filter));
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found")
        );
    }

    private Disbursement getIfExists(UUID id) {
        return disbursementRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Disbursement with id " + id + " not found")
        );
    }
}
