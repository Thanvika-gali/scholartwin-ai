package com.aischolartwin.dto;

public record DimensionScoreDto(
        String dimensionKey,
        String dimensionName,
        int score,
        int maxScore,
        double percentage,
        String criteriaDescription,
        String evidenceCited,
        String status // "EXCELLENT", "GOOD", "NEEDS_IMPROVEMENT", "CRITICAL"
) {}

