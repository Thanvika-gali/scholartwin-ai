package com.aischolartwin.dto;

public record DSAAttemptStatsResponse(
        long totalAttempts,
        long successfulAttempts,
        long failedAttempts,
        double successRatePercent,
        Double averageTimeSpentMinutes
) {
}

