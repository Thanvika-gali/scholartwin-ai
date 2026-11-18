package com.aischolartwin.dto.intelligence;

import java.util.List;
import java.util.Map;

public class IntelligenceAnalyticsDtos {

    public record CompositeIndexPillarDto(
            String pillarName,
            double rawScore,
            double weightPercentage,
            double contributionScore,
            boolean hasData,
            String statusNote
    ) {}

    public record LearningIntelligenceDto(
            double totalStudyHours,
            int topicsCompleted,
            int completedTasks,
            int totalSessions,
            int streakDays,
            int roadmapCompletionRate,
            int completionRate,
            double assessmentAverageScore,
            double revisionRetentionRate,
            double revisionAccuracyPercentage
    ) {}

    public record DSAIntelligenceDto(
            int totalProblemsSolved,
            int totalSolved,
            int totalAttempted,
            int easySolved,
            int easyCount,
            int mediumSolved,
            int mediumCount,
            int hardSolved,
            int hardCount,
            double accuracyPercentage,
            double overallSolveRate,
            String topTopic,
            String weakestTopic,
            Map<String, Integer> topicBreakdown
    ) {}

    public record CareerIntelligenceDto(
            int latestResumeAtsScore,
            int careerReadinessPercentage,
            int currentReadinessScore,
            int verifiedSkillsCount,
            int targetSkillsCount,
            int missingSkillsCount,
            double skillCoveragePercentage,
            String targetRole,
            List<String> topSkillGaps
    ) {}

    public record AIUsageIntelligenceDto(
            long totalConversationsCount,
            long totalQueries,
            long activeMemoriesCount,
            long pinnedMemoriesCount,
            long generatedInsightsCount,
            long pendingRecommendationsCount,
            int groundingRatePercentage,
            long groundingsUsed,
            long averageResponseTimeMs,
            List<String> frequentlyQueriedTopics
    ) {}

    public record IntelligenceOverviewResponse(
            int overallReadinessScore,
            String calculationPolicy,
            List<CompositeIndexPillarDto> compositeBreakdown,
            LearningIntelligenceDto learning,
            DSAIntelligenceDto dsa,
            CareerIntelligenceDto career,
            AIUsageIntelligenceDto aiUsage,
            LearningIntelligenceDto learningVelocity,
            DSAIntelligenceDto dsaMastery,
            CareerIntelligenceDto careerReadiness,
            List<String> topStrengths,
            List<String> topWeaknesses,
            List<String> priorityActions
    ) {}
}
