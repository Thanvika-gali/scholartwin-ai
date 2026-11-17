package com.aischolartwin.dto;

import com.aischolartwin.entity.ResourceType;

import java.time.Instant;

public record ResourceRelationResponse(
        Long id,
        ResourceType targetType,
        Long targetId,
        String targetTitle,
        String relationType,
        String reason,
        boolean isManual,
        Instant createdAt
) {}

