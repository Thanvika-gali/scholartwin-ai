package com.aischolartwin.dto;

import java.util.List;

public record DSARevisionQueueResponse(
        List<DSARevisionResponse> overdue,
        List<DSARevisionResponse> dueToday,
        List<DSARevisionResponse> upcoming,
        List<DSARevisionResponse> completed,
        long totalPendingCount
) {
}

