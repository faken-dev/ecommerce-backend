package com.ecommerce.auth.infrastructure.security;

import com.ecommerce.auth.application.port.DummyPasswordChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Dummy BCrypt check — runs constant-time comparison so that
 * "user not found" and "wrong password" cases take the same time.
 * Uses a pre-computed BCrypt hash of a dummy password.
 */
@Component
public class DummyPasswordCheckerImpl implements DummyPasswordChecker {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Pre-computed BCrypt hash — costs ~60ms (same as real password check)
    // Attacker cannot tell if this is dummy or real
    private static final String DUMMY_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.pZ3p3.xCPj3.o3r2qK";

    @Override
    public void dummyCheck(String rawPassword) {
        // BCrypt constant-time check — same duration as real check
        encoder.matches(rawPassword, DUMMY_HASH);
    }
}