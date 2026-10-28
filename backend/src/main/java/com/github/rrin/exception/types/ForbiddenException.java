package com.github.rrin.exception.types;

import com.github.rrin.exception.BaseException;
import org.springframework.http.HttpStatus;

// The caller is known, but is not allowed to act (e.g. a Telegram account not linked to any employee)
public class ForbiddenException extends BaseException {
    public ForbiddenException(String... messages) {
        super(HttpStatus.FORBIDDEN, "Forbidden", messages);
    }
}
