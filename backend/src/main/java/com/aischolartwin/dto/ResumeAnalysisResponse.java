package com.aischolartwin.dto;

import java.time.Instant;
import java.util.List;

public record ResumeAnalysisResponse(
        Long id,
        Long resumeId,
        String resumeTitle,
        int overallScore,
        int technicalSkillsScore,
        int projectsDepthScore,
        int experienceScore,
        int measurableResultsScore,
        int certificationsScore,
        int structureReadabilityScore,
        int roleRelevanceScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> missingInformation,
        List<String> actionableRecommendations,
        List<DimensionScoreDto> dimensionBreakdown,
        String scoringMethod,
        Instant createdAt
) {}

