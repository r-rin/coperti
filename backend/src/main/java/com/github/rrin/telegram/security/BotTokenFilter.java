package com.github.rrin.telegram.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Lets a request through to /api/bot/** only when it carries the shared bot secret. The bot is the only party that
 * holds it, so the Telegram user id the bot forwards can be trusted: Telegram itself authenticated that user.
 */
public class BotTokenFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "X-Bot-Token";

    private final byte[] expected;

    public BotTokenFilter(String token) {
        this.expected = token == null ? new byte[0] : token.strip().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // fail closed: without a configured secret nobody gets in, rather than everybody
        if (expected.length == 0) {
            reject(response, HttpStatus.SERVICE_UNAVAILABLE, "Bot access is not configured on the server");
            return;
        }
        String presented = request.getHeader(TOKEN_HEADER);
        // constant-time comparison, so response timing reveals nothing about the secret
        if (presented == null || !MessageDigest.isEqual(expected, presented.strip().getBytes(StandardCharsets.UTF_8))) {
            reject(response, HttpStatus.UNAUTHORIZED, "Invalid or missing bot token");
            return;
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        // same plain "[message]" body shape GlobalExceptionHandler produces
        response.setStatus(status.value());
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("[" + message + "]");
    }
}
