package com.github.rrin.exception.types;

import com.github.rrin.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    public ValidationException(String... messages) {
        super(HttpStatus.BAD_REQUEST, "Validation failed", messages);
    }
}
