package com.aischolartwin.dto;

import java.util.List;

public record ExtractedResumeDataDto(
        String fullName,
        String email,
        String phone,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        String summary,
        List<String> skills,
        List<EducationItemDto> education,
        List<ExperienceItemDto> experience,
        List<ProjectItemDto> projects,
        List<CertificationItemDto> certifications,
        List<String> achievements
) {
    public record EducationItemDto(
            String institution,
            String degree,
            String fieldOfStudy,
            String graduationYear,
            String gpa
    ) {}

    public record ExperienceItemDto(
            String company,
            String role,
            String duration,
            String description,
            List<String> highlights
    ) {}

    public record ProjectItemDto(
            String title,
            String techStack,
            String description,
            String liveUrl,
            String githubUrl,
            List<String> highlights
    ) {}

    public record CertificationItemDto(
            String name,
            String issuer,
            String issueDate
    ) {}
}

