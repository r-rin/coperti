package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.ForbiddenException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.telegram.TelegramAccount;
import com.github.rrin.telegram.TelegramLinkCode;
import com.github.rrin.telegram.dto.TelegramAccountRequest;
import com.github.rrin.telegram.repository.TelegramAccountRepository;
import com.github.rrin.telegram.repository.TelegramLinkCodeRepository;
import com.github.rrin.telegram.service.implementation.LinkCodeGenerator;
import com.github.rrin.telegram.service.implementation.TelegramAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramAccountServiceImplTest {

    @Mock
    private TelegramAccountRepository accountRepository;

    @Mock
    private TelegramLinkCodeRepository codeRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LinkCodeGenerator codeGenerator;

    @InjectMocks
    private TelegramAccountServiceImpl service;

    private final Employee ann = Employee.builder().id(UUID.randomUUID()).name("Ann").build();
    private final Employee bob = Employee.builder().id(UUID.randomUUID()).name("Bob").build();

    private void stubEmployee(Employee employee) {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
    }

    @Test
    void linkCreatesAccount() {
        stubEmployee(ann);
        when(accountRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
        when(accountRepository.findByEmployee_Id(ann.getId())).thenReturn(Optional.empty());
        when(accountRepository.save(any(TelegramAccount.class))).thenAnswer(i -> i.getArgument(0));

        TelegramAccount account = service.link(new TelegramAccountRequest(ann.getId(), 42L));

        assertEquals(ann, account.getEmployee());
        assertEquals(42L, account.getTelegramUserId());
        assertNotNull(account.getLinkedAt());
    }

    @Test
    void relinkMovesEmployeeToNewTelegramId() {
        stubEmployee(ann);
        TelegramAccount existing = TelegramAccount.builder().employee(ann).telegramUserId(7L).build();
        when(accountRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
        when(accountRepository.findByEmployee_Id(ann.getId())).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(TelegramAccount.class))).thenAnswer(i -> i.getArgument(0));

        assertSame(existing, service.link(new TelegramAccountRequest(ann.getId(), 42L)));
        assertEquals(42L, existing.getTelegramUserId());
    }

    @Test
    void telegramIdOfAnotherEmployeeRejected() {
        stubEmployee(ann);
        when(accountRepository.findByTelegramUserId(42L))
                .thenReturn(Optional.of(TelegramAccount.builder().employee(bob).telegramUserId(42L).build()));

        assertThrows(ConflictException.class, () -> service.link(new TelegramAccountRequest(ann.getId(), 42L)));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void linkValidatesFields() {
        assertThrows(ValidationException.class, () -> service.link(new TelegramAccountRequest(null, -1L)));
    }

    @Test
    void issueLinkCodeReplacesUnusedCodes() {
        stubEmployee(ann);
        TelegramLinkCode old = TelegramLinkCode.builder().code("OLDCODE1").employee(ann).build();
        when(codeRepository.findAllByEmployee_IdAndUsedAtIsNull(ann.getId())).thenReturn(List.of(old));
        when(codeGenerator.next()).thenReturn("TAKEN234", "FRESH567");
        when(codeRepository.existsByCode("TAKEN234")).thenReturn(true);
        when(codeRepository.existsByCode("FRESH567")).thenReturn(false);
        when(codeRepository.save(any(TelegramLinkCode.class))).thenAnswer(i -> i.getArgument(0));

        TelegramLinkCode code = service.issueLinkCode(ann.getId());

        verify(codeRepository).deleteAll(List.of(old));
        assertEquals("FRESH567", code.getCode());
        assertTrue(code.getExpiresAt().isAfter(Instant.now().plus(Duration.ofMinutes(14))));
    }

    @Test
    void redeemLinksTheCallerAndUsesUpTheCode() {
        TelegramLinkCode code = TelegramLinkCode.builder().code("ABCD2345").employee(ann)
                .expiresAt(Instant.now().plusSeconds(60)).build();
        when(codeRepository.findByCode("ABCD2345")).thenReturn(Optional.of(code));
        when(accountRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
        when(accountRepository.findByEmployee_Id(ann.getId())).thenReturn(Optional.empty());
        when(accountRepository.save(any(TelegramAccount.class))).thenAnswer(i -> i.getArgument(0));

        TelegramAccount account = service.redeemLinkCode(" abcd2345 ", 42L);

        assertEquals(ann, account.getEmployee());
        assertNotNull(code.getUsedAt());
    }

    @Test
    void expiredUsedAndUnknownCodesLookTheSame() {
        TelegramLinkCode expired = TelegramLinkCode.builder().code("EXPIRED2").employee(ann)
                .expiresAt(Instant.now().minusSeconds(1)).build();
        TelegramLinkCode used = TelegramLinkCode.builder().code("USEDCODE").employee(ann)
                .expiresAt(Instant.now().plusSeconds(60)).usedAt(Instant.now()).build();
        when(codeRepository.findByCode("EXPIRED2")).thenReturn(Optional.of(expired));
        when(codeRepository.findByCode("USEDCODE")).thenReturn(Optional.of(used));
        when(codeRepository.findByCode("NOSUCH22")).thenReturn(Optional.empty());

        for (String code : List.of("EXPIRED2", "USEDCODE", "NOSUCH22")) {
            ValidationException error = assertThrows(ValidationException.class, () -> service.redeemLinkCode(code, 42L));
            assertEquals(List.of("Link code is invalid or expired"), error.getMessages());
        }
        verify(accountRepository, never()).save(any());
    }

    @Test
    void resolveReturnsLinkedEmployee() {
        when(accountRepository.findByTelegramUserId(42L))
                .thenReturn(Optional.of(TelegramAccount.builder().employee(ann).telegramUserId(42L).build()));
        assertEquals(ann, service.resolve(42L));
    }

    @Test
    void resolveRejectsUnlinkedAccount() {
        when(accountRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
        assertThrows(ForbiddenException.class, () -> service.resolve(42L));
    }
}
