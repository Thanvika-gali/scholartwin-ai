package com.aischolartwin.dto;

import com.aischolartwin.entity.KnowledgeItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating or updating a knowledge item (Note or Link).
 */
public record KnowledgeItemRequest(
    @NotNull(message = "Item type is required (NOTE or LINK)")
    KnowledgeItemType type,

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    String title,

    String content,

    @Size(max = 1000, message = "URL cannot exceed 1000 characters")
    String url,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    String category,

    Long folderId,
    Long connectedDocumentId,
    Integer connectedPageNumber
) {
    public KnowledgeItemRequest(KnowledgeItemType type, String title, String content, String url, String description, String category) {
        this(type, title, content, url, description, category, null, null, null);
    }
}

