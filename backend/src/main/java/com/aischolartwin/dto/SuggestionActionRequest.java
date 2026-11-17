package com.aischolartwin.dto;

import java.util.List;

public record SuggestionActionRequest(
        String action, // "ACCEPT", "REJECT", "DISMISS"
        Long targetFolderId, // optional override
        List<String> tags
) {}

