package com.aischolartwin.dto;

import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a knowledge item returned to the client.
 */
public record KnowledgeItemResponse(
    Long id,
    KnowledgeItemType type,
    String title,
    String content,
    String url,
    String description,
    String category,
    Long folderId,
    boolean isFavorite,
    boolean isPinned,
    boolean isArchived,
    Long connectedDocumentId,
    Integer connectedPageNumber,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static KnowledgeItemResponse fromEntity(KnowledgeItem item) {
        if (item == null) {
            return null;
        }
        return new KnowledgeItemResponse(
            item.getId(),
            item.getType(),
            item.getTitle(),
            item.getContent(),
            item.getUrl(),
            item.getDescription(),
            item.getCategory(),
            item.getFolderId(),
            item.isFavorite(),
            item.isPinned(),
            item.isArchived(),
            item.getConnectedDocumentId(),
            item.getConnectedPageNumber(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }

    public KnowledgeItemResponse(Long id, KnowledgeItemType type, String title, String content, String url, String description, String category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, type, title, content, url, description, category, null, false, false, false, null, null, createdAt, updatedAt);
    }
}

