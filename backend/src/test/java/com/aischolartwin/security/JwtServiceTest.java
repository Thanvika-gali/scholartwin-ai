package com.aischolartwin.security;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "test_secret_key_that_is_at_least_32_bytes_long_1234567890";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid signed token with user claims")
    void shouldGenerateValidToken() {
        User user = new User("Grace Hopper", "grace.hopper@navy.mil", "hashed_pwd", Role.USER);
        user.setId(77L);

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedEmail = jwtService.extractEmail(token);
        assertEquals("grace.hopper@navy.mil", extractedEmail);

        assertTrue(jwtService.isTokenValid(token, "grace.hopper@navy.mil"));
        assertTrue(jwtService.isTokenValid(token, "GRACE.HOPPER@NAVY.MIL")); // Case-insensitive check
    }

    @Test
    @DisplayName("isTokenValid should return false for mismatched email")
    void shouldRejectTokenWithMismatchedEmail() {
        User user = new User("Ada Lovelace", "ada@example.com", "hash", Role.USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);
        assertFalse(jwtService.isTokenValid(token, "someone.else@example.com"));
    }

    @Test
    @DisplayName("isTokenValid should return false for malformed or tampered token")
    void shouldRejectMalformedToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token", "user@example.com"));
        assertFalse(jwtService.isTokenValid("", "user@example.com"));
        assertFalse(jwtService.isTokenValid(null, "user@example.com"));
    }

    @Test
    @DisplayName("isTokenValid should return false for expired token")
    void shouldRejectExpiredToken() {
        // Create a JwtService with negative expiration (already expired)
        JwtService expiredJwtService = new JwtService(secret, -5000);
        User user = new User("Alan Turing", "alan@turing.ac.uk", "hash", Role.USER);
        user.setId(2L);

        String expiredToken = expiredJwtService.generateToken(user);

        assertTrue(jwtService.isTokenExpired(expiredToken));
        assertFalse(jwtService.isTokenValid(expiredToken, "alan@turing.ac.uk"));
    }

    @Test
    @DisplayName("Tampered signature should fail verification")
    void shouldRejectTamperedSignatureToken() {
        User user = new User("Claude Shannon", "shannon@bell-labs.com", "hash", Role.USER);
        user.setId(3L);

        String token = jwtService.generateToken(user);
        // Tamper with the last few signature characters
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertFalse(jwtService.isTokenValid(tampered, "shannon@bell-labs.com"));
    }
}

