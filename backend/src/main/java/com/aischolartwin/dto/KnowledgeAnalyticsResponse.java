package com.aischolartwin.dto;

import java.util.List;
import java.util.Map;

public record KnowledgeAnalyticsResponse(
        long totalDocuments,
        long totalNotes,
        long totalLinks,
        long totalFolders,
        long smartInboxCount,
        long favoritesCount,
        long archivedCount,
        long storageUsedBytes,
        String storageFormatted,
        Map<String, Long> resourcesBySubject,
        Map<String, Long> resourcesByType,
        List<FolderResponse> mostActiveFolders
) {}

