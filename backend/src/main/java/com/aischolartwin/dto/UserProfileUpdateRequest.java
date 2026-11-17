package com.aischolartwin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,
        String institution,
        String degree,
        Integer graduationYear,
        String academicYear,
        Integer currentSemester,
        String bio,
        String avatarUrl,
        String githubUrl,
        String linkedinUrl,
        String portfolioUrl
) {}

