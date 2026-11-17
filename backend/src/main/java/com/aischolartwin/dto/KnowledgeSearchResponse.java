package com.aischolartwin.dto;

import java.util.List;
import java.util.Map;

public record KnowledgeSearchResponse(
        List<ResourceItemResponse> resources,
        long totalCount,
        boolean semanticSearchAvailable,
        String query,
        Map<String, Long> typeFacets
) {}

