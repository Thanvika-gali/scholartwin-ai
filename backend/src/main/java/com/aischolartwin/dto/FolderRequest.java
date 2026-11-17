package com.aischolartwin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderRequest(
        @NotBlank(message = "Folder name cannot be blank")
        @Size(max = 150, message = "Folder name must not exceed 150 characters")
        String name,

        Long parentId,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String icon,
        String color,
        Boolean isPinned,
        Boolean isFavorite
) {}

