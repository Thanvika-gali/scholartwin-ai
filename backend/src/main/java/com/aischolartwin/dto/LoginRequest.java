package com.aischolartwin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user authentication/login requests.
 */
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {
    /**
     * Returns a normalized (trimmed, lowercase) email address.
     */
    public String normalizedEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    /**
     * Mask password to avoid exposing raw credentials in log outputs.
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}

