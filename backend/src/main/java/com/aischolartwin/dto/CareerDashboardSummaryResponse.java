package com.aischolartwin.dto;

import java.util.List;
import java.util.Map;

public record CareerDashboardSummaryResponse(
        long totalResumes,
        ResumeResponse primaryResume,
        Integer latestScore,
        Integer previousScore,
        Integer scoreDifference,
        long totalSkillsCount,
        Map<String, Long> skillsByCategory,
        List<String> strongestCategories,
        RoleReadinessDto targetRoleReadiness,
        long totalJobAnalysesCount,
        List<JobDescriptionAnalysisResponse> recentJobAnalyses,
        long pendingActionsCount,
        long completedActionsCount,
        List<CareerActionResponse> urgentActions,
        List<String> careerInsights
) {
    public Integer primaryResumeScore() {
        return latestScore;
    }

    public Double targetRoleReadinessPercentage() {
        return targetRoleReadiness != null ? targetRoleReadiness.readinessPercentage() : null;
    }

    public String targetRole() {
        return targetRoleReadiness != null ? targetRoleReadiness.roleName() : null;
    }

    public long totalResumesCount() {
        return totalResumes;
    }

    public long inProgressActionsCount() {
        return pendingActionsCount;
    }
}

