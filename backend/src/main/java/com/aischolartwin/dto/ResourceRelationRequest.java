package com.aischolartwin.dto;

import com.aischolartwin.entity.ResourceType;
import jakarta.validation.constraints.NotNull;

public record ResourceRelationRequest(
        @NotNull ResourceType targetType,
        @NotNull Long targetId,
        String relationType,
        String reason
) {}

