package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.repository.FundingRepository;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.ExpenseService;
import com.github.rrin.expense.service.implementation.FundingServiceImpl;
import com.github.rrin.identity.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundingServiceImplTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private DisbursementService disbursementService;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private FundingServiceImpl service;

    private final UUID disbursementId = UUID.randomUUID();
    private final UUID expenseId = UUID.randomUUID();
    private final Employee worker = Employee.builder().id(UUID.randomUUID()).name("Worker").build();

    private Disbursement disbursement(String amount, DisbursementStatus status) {
        return Disbursement.builder().id(disbursementId).employee(worker).amount(new BigDecimal(amount)).status(status).build();
    }

    private Expense expense(String amount) {
        return Expense.builder().id(expenseId).employee(worker).amount(new BigDecimal(amount)).build();
    }

    private FundingRequest request(String amount) {
        return FundingRequest.builder()
                .disbursementId(disbursementId)
                .expenseId(expenseId)
                .amountCovered(new BigDecimal(amount))
                .build();
    }

    @Test
    void remainingBalanceIsDisbursementMinusDrawnFunding() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("100.00", DisbursementStatus.OPEN));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("70.00"), service.getRemainingBalance(disbursementId));
    }

    @Test
    void shortfallIsExpenseMinusCoverage() {
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(new BigDecimal("30.00"));

        assertEquals(new BigDecimal("20.00"), service.getShortfall(expenseId));
    }

    @Test
    void shortfallNeverGoesNegative() {
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(new BigDecimal("60.00"));

        assertEquals(BigDecimal.ZERO, service.getShortfall(expenseId));
    }

    @Test
    void partialDrawLeavesDisbursementOpen() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("100.00", DisbursementStatus.OPEN));
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.saveAndFlush(any(Funding.class))).thenAnswer(i -> i.getArgument(0));

        Funding funding = service.create(request("30.00"));

        assertEquals(new BigDecimal("30.00"), funding.getAmountCovered());
        verify(disbursementService, never()).updateStatus(any(), any());
    }

    @Test
    void drainingTheDisbursementClosesIt() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("30.00", DisbursementStatus.OPEN));
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("30.00"));
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.saveAndFlush(any(Funding.class))).thenAnswer(i -> i.getArgument(0));

        service.create(request("30.00"));

        verify(disbursementService).updateStatus(disbursementId, DisbursementStatus.CLOSED);
    }

    @Test
    void cannotDrawMoreThanRemainingBalance() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("100.00", DisbursementStatus.OPEN));
        when(expenseService.get(expenseId)).thenReturn(expense("500.00"));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(new BigDecimal("80.00"));
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(BigDecimal.ZERO);

        assertThrows(ConflictException.class, () -> service.create(request("30.00")));
        verify(fundingRepository, never()).saveAndFlush(any(Funding.class));
    }

    @Test
    void cannotCoverMoreThanTheExpenseStillNeeds() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("500.00", DisbursementStatus.OPEN));
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(new BigDecimal("40.00"));

        assertThrows(ConflictException.class, () -> service.create(request("30.00")));
        verify(fundingRepository, never()).saveAndFlush(any(Funding.class));
    }

    @Test
    void cannotDrawFromAClosedDisbursement() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("100.00", DisbursementStatus.CLOSED));
        when(expenseService.get(expenseId)).thenReturn(expense("50.00"));
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(BigDecimal.ZERO);

        assertThrows(ConflictException.class, () -> service.create(request("30.00")));
        verify(fundingRepository, never()).saveAndFlush(any(Funding.class));
    }

    @Test
    void cannotCoverAnotherEmployeesExpense() {
        Expense someoneElses = Expense.builder()
                .id(expenseId)
                .employee(Employee.builder().id(UUID.randomUUID()).name("Other").build())
                .amount(new BigDecimal("50.00"))
                .build();
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement("100.00", DisbursementStatus.OPEN));
        when(expenseService.get(expenseId)).thenReturn(someoneElses);
        when(fundingRepository.sumAmountCoveredByDisbursementId(disbursementId)).thenReturn(BigDecimal.ZERO);
        when(fundingRepository.sumAmountCoveredByExpenseId(expenseId)).thenReturn(BigDecimal.ZERO);

        assertThrows(ConflictException.class, () -> service.create(request("30.00")));
        verify(fundingRepository, never()).saveAndFlush(any(Funding.class));
    }

    @Test
    void amountCoveredMustBePositive() {
        assertThrows(ValidationException.class, () -> service.create(request("0.00")));
        verify(fundingRepository, never()).saveAndFlush(any(Funding.class));
    }
}
