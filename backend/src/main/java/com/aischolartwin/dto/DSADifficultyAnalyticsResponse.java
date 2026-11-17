package com.aischolartwin.dto;

public record DSADifficultyAnalyticsResponse(
        DifficultyMetric easy,
        DifficultyMetric medium,
        DifficultyMetric hard
) {
    public record DifficultyMetric(
            long total,
            long solved,
            long attempted,
            long notStarted,
            double completionRatePercent
    ) {
    }
}

