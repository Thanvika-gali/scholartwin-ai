package com.aischolartwin.dto.learning;

import java.util.List;
import java.util.Map;

public class LearningGapReportDto {

    public record GapItem(
            String topic,
            String category,
            String proficiency,
            String gapLevel, // HIGH, MEDIUM, LOW
            String reason,
            String sourceModule // "CAREER_SKILLS", "DSA_TRACKER", "RESUME_ANALYSIS", "ASSESSMENTS", "KNOWLEDGE_HUB"
    ) {}

    public record StrengthItem(
            String topic,
            String category,
            String proficiency,
            String evidence,
            String sourceModule
    ) {}

    public record RevisionNeedItem(
            String topic,
            int daysOverdue,
            String importance,
            String reason
    ) {}

    public record Recommendation(
            int priorityRank,
            String actionTitle,
            String topic,
            String category,
            String rationale,
            int estimatedMinutesToClose,
            String suggestedActionType // "STUDY_TOPIC", "PRACTICE_DSA", "TAKE_QUIZ", "REVISE"
    ) {}

    public record DataSourceStatus(
            String moduleName,
            boolean isAvailable,
            int recordsFound,
            String description
    ) {}

    public record Response(
            String targetRole,
            int overallReadinessPercentage,
            List<StrengthItem> strongTopics,
            List<GapItem> weakTopics,
            List<String> missingSkills,
            List<RevisionNeedItem> topicsRequiringRevision,
            List<String> prerequisiteTopics,
            List<Recommendation> prioritizedRecommendations,
            List<DataSourceStatus> dataSources,
            Map<String, Integer> topicMasteryDistribution
    ) {}
}
