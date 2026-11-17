package com.aischolartwin.dto;

import java.util.List;

public record ResourceBulkActionRequest(
        List<CollectionItemRequest> items,
        String action, // "MOVE", "TAG", "FAVORITE", "UNFAVORITE", "ARCHIVE", "UNARCHIVE", "DELETE"
        Long targetFolderId,
        List<String> tags
) {}

