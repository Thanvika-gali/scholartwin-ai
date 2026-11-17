package com.aischolartwin.dto;

import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;

import java.time.Instant;
import java.util.Set;

public record DSAProblemResponse(
        Long id,
        String title,
        String problemUrl,
        String platform,
        DSADifficulty difficulty,
        String primaryTopic,
        Set<String> additionalTopics,
        DSAStatus status,
        String personalNotes,
        String solutionApproach,
        String timeComplexity,
        String spaceComplexity,
        boolean revisionRequired,
        Instant createdAt,
        Instant updatedAt,
        Instant solvedAt,
        int attemptCount,
        double successRatePercent
) {
    public static DSAProblemResponse fromEntity(DSAProblem problem, int attemptCount, double successRatePercent) {
        return new DSAProblemResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getProblemUrl(),
                problem.getPlatform(),
                problem.getDifficulty(),
                problem.getPrimaryTopic(),
                problem.getAdditionalTopics(),
                problem.getStatus(),
                problem.getPersonalNotes(),
                problem.getSolutionApproach(),
                problem.getTimeComplexity(),
                problem.getSpaceComplexity(),
                problem.isRevisionRequired(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                problem.getSolvedAt(),
                attemptCount,
                successRatePercent
        );
    }

    public static DSAProblemResponse fromEntity(DSAProblem problem) {
        return fromEntity(problem, 0, 0.0);
    }
}

