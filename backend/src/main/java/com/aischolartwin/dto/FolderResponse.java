package com.aischolartwin.dto;

import java.time.Instant;

public record FolderResponse(
        Long id,
        String name,
        Long parentId,
        String parentName,
        String description,
        String icon,
        String color,
        boolean isPinned,
        boolean isFavorite,
        long documentCount,
        long noteCount,
        long linkCount,
        long totalCount,
        Instant createdAt,
        Instant updatedAt
) {}

