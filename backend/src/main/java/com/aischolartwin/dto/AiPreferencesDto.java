package com.aischolartwin.dto;

public record AiPreferencesDto(
        String interactionMode, // "CONCISE" | "BALANCED" | "DETAILED"
        String groundingMode,   // "STRICT" | "HYBRID"
        Boolean proactiveSuggestions
) {}

