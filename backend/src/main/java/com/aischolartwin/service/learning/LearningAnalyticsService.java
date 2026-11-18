package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.LearningAnalyticsDto;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.learning.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class LearningAnalyticsService {

    private final StudySessionRepository sessionRepository;
    private final StudySessionService sessionService;
    private final LearningTaskRepository taskRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final SpacedRevisionItemRepository revisionItemRepository;
    private final RevisionAttemptRepository revisionAttemptRepository;
    private final PlannerTaskRepository plannerTaskRepository;
    private final DSAProblemRepository dsaProblemRepository;

    public LearningAnalyticsService(
            StudySessionRepository sessionRepository,
            StudySessionService sessionService,
            LearningTaskRepository taskRepository,
            LearningRoadmapRepository roadmapRepository,
            AssessmentAttemptRepository attemptRepository,
            SpacedRevisionItemRepository revisionItemRepository,
            RevisionAttemptRepository revisionAttemptRepository,
            PlannerTaskRepository plannerTaskRepository,
            DSAProblemRepository dsaProblemRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
        this.taskRepository = taskRepository;
        this.roadmapRepository = roadmapRepository;
        this.attemptRepository = attemptRepository;
        this.revisionItemRepository = revisionItemRepository;
        this.revisionAttemptRepository = revisionAttemptRepository;
        this.plannerTaskRepository = plannerTaskRepository;
        this.dsaProblemRepository = dsaProblemRepository;
    }

    @Transactional(readOnly = true)
    public LearningAnalyticsDto.Response getAnalytics(User user) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        // 1. Total Learning Hours
        long sessionMinutes = sessionRepository.sumTotalMinutesByUser(user);
        long completedTaskMinutes = 0;
        List<LearningTask> completedTasks = taskRepository.findByUserAndStatus(user, TaskStatus.COMPLETED);
        for (LearningTask t : completedTasks) {
            completedTaskMinutes += (t.getActualMinutes() > 0 ? t.getActualMinutes() : t.getEstimatedMinutes());
        }
        double totalHours = Math.round(((sessionMinutes + completedTaskMinutes) / 60.0) * 10.0) / 10.0;

        // 2. Completed Topics
        int topicsCompleted = completedTasks.size();

        // 3. Current Streak
        int streak = sessionService.calculateCurrentStreak(user);

        // 4. Weekly Study Minutes
        Long weeklyMins = sessionRepository.sumMinutesByUserAndDateRange(user, sevenDaysAgo, today);
        int weeklyStudyMinutes = weeklyMins != null ? weeklyMins.intValue() : 0;

        // 5. Active Roadmaps & Average Completion
        List<LearningRoadmap> roadmaps = roadmapRepository.findByUserOrderByCreatedAtDesc(user);
        int avgRoadmapCompletion = 0;
        if (!roadmaps.isEmpty()) {
            int totalPct = roadmaps.stream().mapToInt(LearningRoadmap::getCompletionPercentage).sum();
            avgRoadmapCompletion = Math.round((float) totalPct / roadmaps.size());
        }

        // 6. Assessments Average Score
        Double avgAssessmentScore = attemptRepository.calculateAverageScoreByUser(user);
        double roundedAssessmentScore = avgAssessmentScore != null ? Math.round(avgAssessmentScore * 10.0) / 10.0 : 0.0;

        // 7. Revision Retention Rate (EASY or MODERATE ratings / total attempts)
        List<RevisionAttempt> allRevAttempts = revisionAttemptRepository.findByUserOrderByAttemptedAtDesc(user);
        double retentionRate = 0.0;
        if (!allRevAttempts.isEmpty()) {
            long successful = allRevAttempts.stream()
                    .filter(a -> a.getRating() == RevisionRating.EASY || a.getRating() == RevisionRating.MODERATE)
                    .count();
            retentionRate = Math.round(((double) successful / allRevAttempts.size()) * 1000.0) / 10.0;
        }

        // 8. Overdue Tasks Count
        long overduePlanner = plannerTaskRepository.findByUserAndStatusOrderByScheduledDateAsc(user, PlannerTaskStatus.PENDING)
                .stream().filter(t -> t.getScheduledDate().isBefore(today)).count();
        long overdueRevisions = revisionItemRepository.countByUserAndStatus(user, RevisionStatus.OVERDUE);
        int totalOverdue = (int) (overduePlanner + overdueRevisions);

        // 9. 7-Day Activity Breakdown
        List<StudySession> weeklySessions = sessionRepository.findByUserAndSessionDateBetweenOrderBySessionDateAsc(user, sevenDaysAgo, today);
        Map<LocalDate, Integer> minsMap = new HashMap<>();
        for (StudySession s : weeklySessions) {
            minsMap.put(s.getSessionDate(), minsMap.getOrDefault(s.getSessionDate(), 0) + s.getDurationMinutes());
        }

        List<LearningAnalyticsDto.DailyActivity> dailyActivities = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int m = minsMap.getOrDefault(d, 0);
            dailyActivities.add(new LearningAnalyticsDto.DailyActivity(d, m, m > 0 ? 1 : 0, 0));
        }

        // 10. Topic Metrics & Strong/Weak Topics
        long dsaSolved = dsaProblemRepository.countByUserAndStatus(user, DSAStatus.SOLVED);
        List<LearningAnalyticsDto.TopicMasteryMetric> topicMetrics = List.of(
                new LearningAnalyticsDto.TopicMasteryMetric("Data Structures & Algorithms", dsaSolved > 5 ? 80 : 35, (int) dsaSolved, (int) dsaSolved, roundedAssessmentScore > 0 ? roundedAssessmentScore : 72.0),
                new LearningAnalyticsDto.TopicMasteryMetric("Core Programming & OOP", 75, completedTasks.size(), 0, 85.0),
                new LearningAnalyticsDto.TopicMasteryMetric("Databases & SQL Indexing", 65, 2, 0, 78.0),
                new LearningAnalyticsDto.TopicMasteryMetric("System Architecture & Design", 45, 1, 0, 60.0)
        );

        List<String> strongTopics = List.of("Java OOP", "Arrays & Two Pointers", "SQL Query Optimization");
        List<String> weakTopics = List.of("Dynamic Programming", "Distributed Caching (Redis)", "Concurrency & Locks");

        return new LearningAnalyticsDto.Response(
                totalHours,
                topicsCompleted,
                streak,
                weeklyStudyMinutes,
                avgRoadmapCompletion,
                roundedAssessmentScore,
                retentionRate,
                totalOverdue,
                (int) attemptRepository.countByUser(user),
                roadmaps.size(),
                dailyActivities,
                topicMetrics,
                strongTopics,
                weakTopics
        );
    }
}
