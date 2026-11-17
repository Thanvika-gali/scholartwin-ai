package com.aischolartwin.dto;

import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.SuggestionStatus;

import java.time.Instant;
import java.util.List;

public record OrganizationSuggestionResponse(
        Long id,
        ResourceType resourceType,
        Long resourceId,
        String resourceTitle,
        Long suggestedFolderId,
        String suggestedFolderName,
        String suggestedFolderPath,
        String suggestedSubject,
        Integer suggestedSemester,
        String suggestedCategory,
        List<String> suggestedTags,
        double confidenceScore,
        String reasoning,
        boolean isAiGenerated,
        SuggestionStatus status,
        Instant createdAt
) {}

