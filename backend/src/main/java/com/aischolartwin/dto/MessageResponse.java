package com.aischolartwin.dto;

/**
 * Simple message response representation.
 */
public record MessageResponse(String message) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}

