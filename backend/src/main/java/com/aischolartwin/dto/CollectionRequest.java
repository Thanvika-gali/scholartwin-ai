package com.aischolartwin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionRequest(
        @NotBlank(message = "Collection name is required")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        String color,
        String icon
) {}

