package com.github.rrin.telegram.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ForbiddenException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.telegram.TelegramAccount;
import com.github.rrin.telegram.TelegramLinkCode;
import com.github.rrin.telegram.dto.TelegramAccountRequest;
import com.github.rrin.telegram.repository.TelegramAccountRepository;
import com.github.rrin.telegram.repository.TelegramLinkCodeRepository;
import com.github.rrin.telegram.service.TelegramAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TelegramAccountServiceImpl implements TelegramAccountService {

    static final Duration CODE_LIFETIME = Duration.ofMinutes(15);

    // one message for every way a code can be wrong, so guessing gets no hint
    static final String BAD_CODE = "Link code is invalid or expired";

    private final TelegramAccountRepository accountRepository;
    private final TelegramLinkCodeRepository codeRepository;
    private final EmployeeRepository employeeRepository;
    private final LinkCodeGenerator codeGenerator;

    @Autowired
    public TelegramAccountServiceImpl(TelegramAccountRepository accountRepository,
                                      TelegramLinkCodeRepository codeRepository,
                                      EmployeeRepository employeeRepository,
                                      LinkCodeGenerator codeGenerator) {
        this.accountRepository = accountRepository;
        this.codeRepository = codeRepository;
        this.employeeRepository = employeeRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public List<TelegramAccount> getAll() {
        return accountRepository.findAll();
    }

    @Override
    public TelegramAccount getForEmployee(UUID employeeId) {
        return accountRepository.findByEmployee_Id(getEmployee(employeeId).getId()).orElseThrow(
                () -> new EntityNotFoundException("Employee " + employeeId + " has no linked Telegram account"));
    }

    @Override
    @Transactional
    public TelegramAccount link(TelegramAccountRequest request) {
        new ValidationCheck()
                .check(request.getEmployeeId() != null, "Employee id is required")
                .check(request.getTelegramUserId() != null && request.getTelegramUserId() > 0,
                        "Telegram user id must be a positive number")
                .throwIfAny(ValidationException::new);
        return linkEmployee(getEmployee(request.getEmployeeId()), request.getTelegramUserId());
    }

    @Override
    public TelegramAccount unlink(UUID employeeId) {
        TelegramAccount account = getForEmployee(employeeId);
        accountRepository.delete(account);
        return account;
    }

    @Override
    @Transactional
    public TelegramLinkCode issueLinkCode(UUID employeeId) {
        Employee employee = getEmployee(employeeId);
        codeRepository.deleteAll(codeRepository.findAllByEmployee_IdAndUsedAtIsNull(employeeId));

        String code;
        do {
            code = codeGenerator.next();
        } while (codeRepository.existsByCode(code));

        return codeRepository.save(TelegramLinkCode.builder()
                .code(code)
                .employee(employee)
                .expiresAt(Instant.now().plus(CODE_LIFETIME))
                .build());
    }

    @Override
    @Transactional
    public TelegramAccount redeemLinkCode(String code, long telegramUserId) {
        new ValidationCheck()
                .check(code != null && !code.isBlank(), "Link code is required")
                .throwIfAny(ValidationException::new);

        Instant now = Instant.now();
        TelegramLinkCode linkCode = codeRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .filter(found -> found.getUsedAt() == null && found.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new ValidationException(BAD_CODE));

        TelegramAccount account = linkEmployee(linkCode.getEmployee(), telegramUserId);
        linkCode.setUsedAt(now);
        codeRepository.save(linkCode);
        return account;
    }

    @Override
    public Employee resolve(long telegramUserId) {
        return accountRepository.findByTelegramUserId(telegramUserId)
                .map(TelegramAccount::getEmployee)
                .orElseThrow(() -> new ForbiddenException(
                        "Telegram account " + telegramUserId + " is not linked to an employee"));
    }

    private TelegramAccount linkEmployee(Employee employee, long telegramUserId) {
        new ValidationCheck()
                .check(accountRepository.findByTelegramUserId(telegramUserId)
                                .map(existing -> existing.getEmployee().getId().equals(employee.getId()))
                                .orElse(true),
                        "Telegram account " + telegramUserId + " is already linked to another employee")
                .throwIfAny(ConflictException::new);

        // an employee has one account: linking again moves them to the new Telegram id
        TelegramAccount account = accountRepository.findByEmployee_Id(employee.getId())
                .orElseGet(() -> TelegramAccount.builder().employee(employee).build());
        account.setTelegramUserId(telegramUserId);
        account.setLinkedAt(Instant.now());
        return accountRepository.save(account);
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found"));
    }
}
