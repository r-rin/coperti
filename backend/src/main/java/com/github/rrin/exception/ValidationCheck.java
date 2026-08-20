package com.github.rrin.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ValidationCheck {
    public List<String> messages;

    public ValidationCheck(List<String> messages) {
        this.messages = messages;
    }

    public ValidationCheck() {
        this.messages = new ArrayList<>();
    }

    public ValidationCheck check(boolean condition, String message) {
        if (!condition) {
            messages.add(message);
        }
        return this;
    }

    public <E extends BaseException> void throwIfAny(Function<String[], E> exceptionFactory) throws E {
        if (!messages.isEmpty()) {
            throw exceptionFactory.apply(messages.toArray(new String[0]));
        }
    }
}
