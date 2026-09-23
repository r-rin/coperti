package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.AllocationRequest;
import com.github.rrin.expense.dto.AllocationSelection;
import com.github.rrin.expense.dto.FundingRequest;
import com.github.rrin.expense.service.AllocationResult;
import com.github.rrin.expense.service.DisbursementService;
import com.github.rrin.expense.service.FundingService;
import com.github.rrin.expense.service.implementation.AllocationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {

    @Mock
    private FundingService fundingService;

    @Mock
    private DisbursementService disbursementService;

    @InjectMocks
    private AllocationServiceImpl service;

    @Captor
    private ArgumentCaptor<FundingRequest> fundingCaptor;

    private final UUID disbursementId = UUID.randomUUID();
    private final UUID expenseA = UUID.randomUUID();
    private final UUID expenseB = UUID.randomUUID();

    private Disbursement disbursement(DisbursementStatus status) {
        return Disbursement.builder().id(disbursementId).amount(new BigDecimal("500.00")).status(status).build();
    }

    private AllocationRequest request(AllocationSelection... selections) {
        return AllocationRequest.builder()
                .disbursementId(disbursementId)
                .selections(List.of(selections))
                .build();
    }

    private AllocationSelection selection(UUID expenseId, String amount) {
        return AllocationSelection.builder().expenseId(expenseId).amount(new BigDecimal(amount)).build();
    }

    @Test
    void coversSeveralExpensesFromOneDisbursement() {
        when(disbursementService.getById(disbursementId))
                .thenReturn(disbursement(DisbursementStatus.OPEN), disbursement(DisbursementStatus.OPEN));
        when(fundingService.getRemainingBalance(disbursementId))
                .thenReturn(new BigDecimal("500.00"), new BigDecimal("79.50"));
        when(fundingService.create(any(FundingRequest.class))).thenReturn(Funding.builder().build());

        AllocationResult result = service.allocateManual(
                request(selection(expenseA, "300.00"), selection(expenseB, "120.50")));

        assertEquals(2, result.fundingsCreated().size());
        assertEquals(new BigDecimal("79.50"), result.remainingBalance());
        assertEquals(DisbursementStatus.OPEN, result.disbursementStatus());

        verify(fundingService, org.mockito.Mockito.times(2)).create(fundingCaptor.capture());
        assertEquals(expenseA, fundingCaptor.getAllValues().get(0).getExpenseId());
        assertEquals(new BigDecimal("300.00"), fundingCaptor.getAllValues().get(0).getAmountCovered());
        assertEquals(expenseB, fundingCaptor.getAllValues().get(1).getExpenseId());
    }

    @Test
    void reportsTheClosedStatusWhenTheSelectionDrainsTheAdvance() {
        when(disbursementService.getById(disbursementId))
                .thenReturn(disbursement(DisbursementStatus.OPEN), disbursement(DisbursementStatus.CLOSED));
        when(fundingService.getRemainingBalance(disbursementId))
                .thenReturn(new BigDecimal("500.00"), BigDecimal.ZERO);
        when(fundingService.create(any(FundingRequest.class))).thenReturn(Funding.builder().build());

        AllocationResult result = service.allocateManual(request(selection(expenseA, "500.00")));

        assertEquals(DisbursementStatus.CLOSED, result.disbursementStatus());
        assertEquals(BigDecimal.ZERO, result.remainingBalance());
    }

    @Test
    void wholeSelectionIsRejectedWhenTheTotalOverdrawsTheAdvance() {
        when(disbursementService.getById(disbursementId)).thenReturn(disbursement(DisbursementStatus.OPEN));
        when(fundingService.getRemainingBalance(disbursementId)).thenReturn(new BigDecimal("400.00"));

        assertThrows(ConflictException.class, () -> service.allocateManual(
                request(selection(expenseA, "300.00"), selection(expenseB, "120.50"))));

        // nothing is written: the first row alone would have fitted inside the remaining balance
        verify(fundingService, never()).create(any(FundingRequest.class));
    }

    @Test
    void rejectsTheSameExpenseSelectedTwice() {
        assertThrows(ValidationException.class, () -> service.allocateManual(
                request(selection(expenseA, "10.00"), selection(expenseA, "20.00"))));
        verify(fundingService, never()).create(any(FundingRequest.class));
    }

    @Test
    void rejectsAnEmptySelection() {
        assertThrows(ValidationException.class, () -> service.allocateManual(
                AllocationRequest.builder().disbursementId(disbursementId).selections(List.of()).build()));
        verify(fundingService, never()).create(any(FundingRequest.class));
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThrows(ValidationException.class, () -> service.allocateManual(
                request(selection(expenseA, "-5.00"))));
        verify(fundingService, never()).create(any(FundingRequest.class));
    }
}
