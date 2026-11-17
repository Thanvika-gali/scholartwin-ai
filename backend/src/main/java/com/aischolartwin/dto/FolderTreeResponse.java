package com.aischolartwin.dto;

import java.time.Instant;
import java.util.List;

public record FolderTreeResponse(
        Long id,
        String name,
        Long parentId,
        String description,
        String icon,
        String color,
        boolean isPinned,
        boolean isFavorite,
        long documentCount,
        long noteCount,
        long linkCount,
        long totalCount,
        List<FolderTreeResponse> children,
        Instant createdAt,
        Instant updatedAt
) {}

