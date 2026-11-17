package com.aischolartwin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for profile update requests.
 * Only permits updating safe, non-sensitive profile attributes (full name).
 */
public record UpdateProfileRequest(
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName
) {
}

