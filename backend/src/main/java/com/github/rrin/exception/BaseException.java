package com.github.rrin.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final List<String> messages;

    protected BaseException(HttpStatus status, String title, String... messages) {
        super(title);
        this.status = status;
        this.messages = List.of(messages);
    }

    protected BaseException(String title, String... messages) {
        super(title);
        this.status = HttpStatus.BAD_REQUEST;
        this.messages = List.of(messages);
    }

    @Override
    public String getMessage() {
        return messages.toString();
    }
}
