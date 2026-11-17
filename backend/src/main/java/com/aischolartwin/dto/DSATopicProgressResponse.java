package com.aischolartwin.dto;

import java.time.LocalDate;

public record DSATopicProgressResponse(
        String topicName,
        long totalProblems,
        long solvedProblems,
        long attemptedProblems,
        long notStartedProblems,
        double completionPercentage,
        LocalDate lastPracticedAt
) {
}

