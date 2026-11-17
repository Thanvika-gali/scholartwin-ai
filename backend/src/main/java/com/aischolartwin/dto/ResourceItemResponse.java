package com.aischolartwin.dto;

import com.aischolartwin.entity.ResourceType;

import java.time.Instant;
import java.util.List;

/**
 * Unified representation of any academic knowledge resource (Document, Note, Link)
 * for seamless workspace rendering, search, filters, collections, and Smart Inbox.
 */
public record ResourceItemResponse(
        ResourceType resourceType,
        Long id,
        String title,
        String description,
        String content,
        String url,
        String category,
        Long folderId,
        String folderName,
        String folderPath,
        String fileExtension,
        String fileSizeFormatted,
        long fileSizeBytes,
        Integer pageCount,
        String processingStatus,
        boolean isFavorite,
        boolean isPinned,
        boolean isArchived,
        List<String> tags,
        String summary,
        int versionNumber,
        Instant createdAt,
        Instant updatedAt,
        Instant lastViewedAt
) {}

