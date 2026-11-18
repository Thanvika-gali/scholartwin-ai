package com.aischolartwin.dto.learning;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LearningAnalyticsDto {

    public record DailyActivity(
            LocalDate date,
            int minutesSpent,
            int tasksCompleted,
            int revisionsDone
    ) {}

    public record TopicMasteryMetric(
            String topic,
            int masteryPercentage,
            int tasksCompleted,
            int dsaProblemsSolved,
            Double averageQuizScore
    ) {}

    public record Response(
            double totalHoursLearned,
            int topicsCompleted,
            int currentStreakDays,
            int weeklyStudyMinutes,
            int roadmapCompletionPercentage,
            double averageAssessmentScore,
            double revisionRetentionRate,
            int overdueTasksCount,
            int totalAssessmentsAttempted,
            int activeRoadmapsCount,
            List<DailyActivity> weeklyActivity,
            List<TopicMasteryMetric> topicMetrics,
            List<String> strongTopics,
            List<String> weakTopics
    ) {}
}
