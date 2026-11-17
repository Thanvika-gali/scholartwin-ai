package com.aischolartwin.dto;

import java.util.List;

public record ResumeUpdateRequest(
        String title,
        String fullName,
        String email,
        String phone,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        String summary,
        List<String> skills,
        String educationJson,
        String experienceJson,
        String projectsJson,
        String certificationsJson,
        String achievementsJson
) {}

