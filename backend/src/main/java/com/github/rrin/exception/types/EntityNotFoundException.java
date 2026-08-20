package com.github.rrin.exception.types;

import com.github.rrin.exception.BaseException;
import org.springframework.http.HttpStatus;

//TODO: add multiple languages support
public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(String... messages) {
        super(HttpStatus.NOT_FOUND, "Source not found", messages);
    }
}
