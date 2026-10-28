package com.github.rrin.telegram.service;

import com.github.rrin.identity.Employee;
import com.github.rrin.telegram.TelegramAccount;
import com.github.rrin.telegram.TelegramLinkCode;
import com.github.rrin.telegram.dto.TelegramAccountRequest;

import java.util.List;
import java.util.UUID;

public interface TelegramAccountService {
    /** Every linked account; small (one per employee at most), so not paged. */
    List<TelegramAccount> getAll();

    TelegramAccount getForEmployee(UUID employeeId);

    /** Administrator links an employee to a known Telegram id; relinking replaces the employee's previous account. */
    TelegramAccount link(TelegramAccountRequest request);

    TelegramAccount unlink(UUID employeeId);

    /** A fresh single-use code for the employee; any earlier unused code stops working. */
    TelegramLinkCode issueLinkCode(UUID employeeId);

    /** The Telegram user proves who they are by sending the code they were given. */
    TelegramAccount redeemLinkCode(String code, long telegramUserId);

    /** The employee a Telegram user acts as; ForbiddenException when the account isn't linked. */
    Employee resolve(long telegramUserId);
}
