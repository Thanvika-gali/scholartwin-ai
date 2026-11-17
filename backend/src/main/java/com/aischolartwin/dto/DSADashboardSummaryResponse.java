package com.aischolartwin.dto;

import java.util.List;

public record DSADashboardSummaryResponse(
        long totalProblems,
        long solvedProblems,
        long attemptedProblems,
        long remainingProblems,
        long easySolved,
        long easyTotal,
        long mediumSolved,
        long mediumTotal,
        long hardSolved,
        long hardTotal,
        double overallAccuracyPercent,
        int currentStreakDays,
        int longestStreakDays,
        long pendingRevisionsCount,
        long overdueRevisionsCount,
        List<DSAProblemResponse> recentlySolved,
        List<DSAAttemptResponse> recentlyAttempted,
        List<DSARevisionResponse> pendingRevisions,
        List<String> insights
) {
}

