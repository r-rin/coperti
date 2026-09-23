package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.repository.DisbursementRepository;
import com.github.rrin.expense.repository.FundingRepository;
import com.github.rrin.expense.service.implementation.DisbursementServiceImpl;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisbursementServiceImplTest {

    @Mock
    private DisbursementRepository disbursementRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private FundingRepository fundingRepository;

    @InjectMocks
    private DisbursementServiceImpl service;

    private Disbursement disbursement(UUID id, DisbursementStatus status) {
        return Disbursement.builder().id(id).amount(new BigDecimal("5000.00")).status(status).build();
    }

    @Test
    void createAlwaysStartsOpen() {
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId))
                .thenReturn(Optional.of(Employee.builder().id(employeeId).name("Worker").build()));
        when(disbursementRepository.save(any(Disbursement.class))).thenAnswer(i -> i.getArgument(0));

        Disbursement created = service.create(DisbursementRequest.builder()
                .employeeId(employeeId)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now())
                .status(DisbursementStatus.CLOSED)
                .build());

        assertEquals(DisbursementStatus.OPEN, created.getStatus());
    }

    @Test
    void settleOpenDisbursement() {
        UUID id = UUID.randomUUID();
        when(disbursementRepository.findById(id)).thenReturn(Optional.of(disbursement(id, DisbursementStatus.OPEN)));
        when(disbursementRepository.save(any(Disbursement.class))).thenAnswer(i -> i.getArgument(0));

        Disbursement settled = service.updateStatus(id, DisbursementStatus.CLOSED);

        assertEquals(DisbursementStatus.CLOSED, settled.getStatus());
    }

    @Test
    void cancelOpenDisbursementWithoutFunding() {
        UUID id = UUID.randomUUID();
        when(disbursementRepository.findById(id)).thenReturn(Optional.of(disbursement(id, DisbursementStatus.OPEN)));
        when(fundingRepository.existsByDisbursementId(id)).thenReturn(false);
        when(disbursementRepository.save(any(Disbursement.class))).thenAnswer(i -> i.getArgument(0));

        Disbursement cancelled = service.updateStatus(id, DisbursementStatus.CANCELLED);

        assertEquals(DisbursementStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void cannotCancelDisbursementThatAlreadyFundedExpenses() {
        UUID id = UUID.randomUUID();
        when(disbursementRepository.findById(id)).thenReturn(Optional.of(disbursement(id, DisbursementStatus.OPEN)));
        when(fundingRepository.existsByDisbursementId(id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.updateStatus(id, DisbursementStatus.CANCELLED));
        verify(disbursementRepository, never()).save(any(Disbursement.class));
    }

    @Test
    void cannotReopenSettledDisbursement() {
        UUID id = UUID.randomUUID();
        when(disbursementRepository.findById(id)).thenReturn(Optional.of(disbursement(id, DisbursementStatus.CLOSED)));

        assertThrows(ConflictException.class, () -> service.updateStatus(id, DisbursementStatus.OPEN));
        verify(disbursementRepository, never()).save(any(Disbursement.class));
    }

    @Test
    void cannotCancelSettledDisbursement() {
        UUID id = UUID.randomUUID();
        when(disbursementRepository.findById(id)).thenReturn(Optional.of(disbursement(id, DisbursementStatus.CLOSED)));

        assertThrows(ConflictException.class, () -> service.updateStatus(id, DisbursementStatus.CANCELLED));
        verify(disbursementRepository, never()).save(any(Disbursement.class));
    }

    @Test
    void statusIsRequired() {
        assertThrows(ValidationException.class, () -> service.updateStatus(UUID.randomUUID(), null));
        verify(disbursementRepository, never()).save(any(Disbursement.class));
    }
}
