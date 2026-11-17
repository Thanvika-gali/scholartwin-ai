package com.aischolartwin.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized API Error Response Envelope.
 */
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String timestamp,
    Map<String, String> validationErrors
) {
    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, Instant.now().toString(), null);
    }

    public static ApiErrorResponse of(int status, String error, String message, Map<String, String> validationErrors) {
        return new ApiErrorResponse(status, error, message, Instant.now().toString(), validationErrors);
    }
}

