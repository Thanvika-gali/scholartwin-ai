package com.aischolartwin.dto;

import java.util.List;

public record BulkOrganizeRequest(
        List<Long> suggestionIds,
        String action // "ACCEPT", "REJECT", "DISMISS"
) {}

