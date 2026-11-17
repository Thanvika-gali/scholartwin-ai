package com.aischolartwin.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DataExportResponse(
        String application,
        String version,
        Instant exportedAt,
        UserResponse userProfile,
        Map<String, Long> summaryCounts,
        List<KnowledgeItemResponse> knowledgeItems,
        List<DocumentResponse> documents,
        List<DSAProblemResponse> dsaProblems,
        List<CareerSkillResponse> careerSkills,
        List<JobDescriptionAnalysisResponse> jobAnalyses,
        List<CareerActionResponse> careerActions
) {}

