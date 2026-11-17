package com.aischolartwin.dto;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a user response.
 * Strictly excludes any credentials or password hashes.
 */
public record UserResponse(
    Long id,
    String fullName,
    String email,
    Role role,
    boolean isActive,
    String institution,
    String degree,
    Integer graduationYear,
    String academicYear,
    Integer currentSemester,
    String bio,
    String avatarUrl,
    String githubUrl,
    String linkedinUrl,
    String portfolioUrl,
    String academicPreferencesJson,
    String aiPreferencesJson,
    String notificationPreferencesJson,
    String themePreference,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * Backwards-compatible constructor for 7-argument calls.
     */
    public UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this(id, fullName, email, role, isActive, null, null, null, null, null, null, null, null, null, null, null, null, null, "SYSTEM", createdAt, updatedAt);
    }

    /**
     * Factory method to safely project a User entity into a UserResponse DTO.
     *
     * @param user The persistent User entity
     * @return UserResponse without sensitive security credentials
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.getInstitution(),
            user.getDegree(),
            user.getGraduationYear(),
            user.getAcademicYear(),
            user.getCurrentSemester(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getGithubUrl(),
            user.getLinkedinUrl(),
            user.getPortfolioUrl(),
            user.getAcademicPreferencesJson(),
            user.getAiPreferencesJson(),
            user.getNotificationPreferencesJson(),
            user.getThemePreference() != null ? user.getThemePreference() : "SYSTEM",
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

