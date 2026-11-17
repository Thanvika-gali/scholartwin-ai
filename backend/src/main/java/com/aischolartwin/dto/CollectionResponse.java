package com.aischolartwin.dto;

import java.time.Instant;
import java.util.List;

public record CollectionResponse(
        Long id,
        String name,
        String description,
        String color,
        String icon,
        long itemCount,
        List<ResourceItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {}

