package com.aischolartwin.dto;

import com.aischolartwin.entity.ResourceType;
import jakarta.validation.constraints.NotNull;

public record CollectionItemRequest(
        @NotNull(message = "Resource type is required")
        ResourceType resourceType,

        @NotNull(message = "Resource id is required")
        Long resourceId
) {}

