package com.aischolartwin.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DocumentIntelligenceResponse(
        Long documentId,
        String title,
        String summary,
        List<String> mainTopics,
        List<String> keyDefinitions,
        List<String> formulas,
        List<String> keyHeadings,
        List<String> suggestedTags,
        String estimatedSubject,
        Map<String, List<Integer>> conceptPageReferences,
        Instant generatedAt
) {}

