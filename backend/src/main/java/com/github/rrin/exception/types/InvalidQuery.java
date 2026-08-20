package com.github.rrin.exception.types;

import com.github.rrin.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidQuery extends BaseException {
    public InvalidQuery(String... messages) {
        super(HttpStatus.BAD_REQUEST, "Invalid query provided", messages);
    }
}
