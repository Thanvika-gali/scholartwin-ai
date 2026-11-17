package com.aischolartwin.dto;

import java.util.List;

/**
 * Data Transfer Object representing the summary metrics and recent activity
 * displayed on the personalized student dashboard.
 */
public record DashboardSummaryResponse(
    long totalItems,
    long totalNotes,
    long totalLinks,
    long totalDocuments,
    List<KnowledgeItemResponse> recentItems
) {
}
