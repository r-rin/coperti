package com.github.rrin.telegram.service.implementation;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Random codes people can read aloud or retype: no 0/O, 1/I/L lookalikes. */
@Component
public class LinkCodeGenerator {

    static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    static final int LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String next() {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
