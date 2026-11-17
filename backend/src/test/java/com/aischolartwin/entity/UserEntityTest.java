package com.aischolartwin.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    @DisplayName("Should initialize user with default role and active status")
    void shouldInitializeWithDefaults() {
        User user = new User("Ada Lovelace", "ada@example.com", "hashed_password", null);
        user.onCreate();

        assertEquals(Role.USER, user.getRole());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals("ada@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Lifecycle hook should normalize email to lowercase and trim whitespace")
    void shouldNormalizeEmailOnPersistAndUpdate() {
        User user = new User("Alan Turing", "  Alan.Turing@Cambridge.Ac.UK  ", "hashed_pwd", Role.USER);
        user.onCreate();

        assertEquals("alan.turing@cambridge.ac.uk", user.getEmail());

        user.setEmail("  NEW.EMAIL@TURING.ORG  ");
        user.onUpdate();
        assertEquals("new.email@turing.org", user.getEmail());
    }

    @Test
    @DisplayName("toString() MUST exclude passwordHash to prevent credential leakage in logs")
    void toStringShouldExcludePasswordHash() {
        String sensitiveHash = "$2a$12$e80yVv..secret_password_hash";
        User user = new User("Grace Hopper", "grace@navy.mil", sensitiveHash, Role.ADMIN);
        user.setId(42L);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        String stringRep = user.toString();

        assertFalse(stringRep.contains(sensitiveHash), "toString() must never contain password hash");
        assertTrue(stringRep.contains("grace@navy.mil"));
        assertTrue(stringRep.contains("Grace Hopper"));
        assertTrue(stringRep.contains("ADMIN"));
    }
}

