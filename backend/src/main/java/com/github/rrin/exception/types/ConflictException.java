package com.github.rrin.exception.types;

import com.github.rrin.exception.BaseException;
import org.springframework.http.HttpStatus;

// The request is well-formed, but the current state of the data does not allow it
public class ConflictException extends BaseException {
    public ConflictException(String... messages) {
        super(HttpStatus.CONFLICT, "Conflict with current state", messages);
    }
}
