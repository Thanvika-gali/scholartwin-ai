package com.aischolartwin.service;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DSAAnalyticsService {

    private final DSAProblemRepository problemRepository;
    private final DSAAttemptRepository attemptRepository;
    private final DSARevisionRepository revisionRepository;

    public DSAAnalyticsService(
            DSAProblemRepository problemRepository,
            DSAAttemptRepository attemptRepository,
            DSARevisionRepository revisionRepository
    ) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional(readOnly = true)
    public DSADashboardSummaryResponse getDashboardSummary(User user) {
        long totalProblems = problemRepository.countByUser(user);
        long solvedProblems = problemRepository.countByUserAndStatus(user, DSAStatus.SOLVED);
        long attemptedProblems = problemRepository.countByUserAndStatus(user, DSAStatus.ATTEMPTED);
        long remainingProblems = Math.max(0, totalProblems - solvedProblems);

        long easySolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.EASY, DSAStatus.SOLVED);
        long easyTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.EASY);

        long mediumSolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.MEDIUM, DSAStatus.SOLVED);
        long mediumTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.MEDIUM);

        long hardSolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.HARD, DSAStatus.SOLVED);
        long hardTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.HARD);

        long totalAttempts = attemptRepository.countByUser(user);
        long successfulAttempts = attemptRepository.countByUserAndOutcome(user, DSAAttemptOutcome.SUCCESS);
        double overallAccuracy = totalAttempts > 0
                ? Math.round(((double) successfulAttempts / totalAttempts) * 1000.0) / 10.0
                : (totalProblems > 0 ? Math.round(((double) solvedProblems / totalProblems) * 1000.0) / 10.0 : 0.0);

        int currentStreak = calculateCurrentStreak(user);
        int longestStreak = calculateLongestStreak(user);

        LocalDate today = LocalDate.now();
        List<DSARevision> allRevisions = revisionRepository.findByUserOrderByScheduledDateAsc(user);
        long pendingRevisionsCount = allRevisions.stream()
                .filter(r -> r.getStatus() == DSARevisionStatus.PENDING)
                .count();
        long overdueRevisionsCount = allRevisions.stream()
                .filter(r -> r.getStatus() != DSARevisionStatus.COMPLETED && r.getScheduledDate().isBefore(today))
                .count();

        List<DSAProblemResponse> recentlySolved = problemRepository.findTop5ByUserAndStatusOrderBySolvedAtDesc(user, DSAStatus.SOLVED)
                .stream()
                .map(DSAProblemResponse::fromEntity)
                .toList();

        List<DSAAttemptResponse> recentlyAttempted = attemptRepository.findTop5ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(DSAAttemptResponse::fromEntity)
                .toList();

        List<DSARevisionResponse> pendingRevisions = allRevisions.stream()
                .filter(r -> r.getStatus() != DSARevisionStatus.COMPLETED)
                .limit(5)
                .map(DSARevisionResponse::fromEntity)
                .toList();

        List<String> insights = generateInsights(user, totalProblems, solvedProblems, easyTotal, easySolved,
                mediumTotal, mediumSolved, hardTotal, hardSolved, currentStreak, overdueRevisionsCount);

        return new DSADashboardSummaryResponse(
                totalProblems,
                solvedProblems,
                attemptedProblems,
                remainingProblems,
                easySolved,
                easyTotal,
                mediumSolved,
                mediumTotal,
                hardSolved,
                hardTotal,
                overallAccuracy,
                currentStreak,
                longestStreak,
                pendingRevisionsCount,
                overdueRevisionsCount,
                recentlySolved,
                recentlyAttempted,
                pendingRevisions,
                insights
        );
    }

    @Transactional(readOnly = true)
    public List<DSATopicProgressResponse> getTopicProgressList(User user) {
        List<DSAProblem> allProblems = problemRepository.findByUserOrderByUpdatedAtDesc(user);
        List<DSAAttempt> allAttempts = attemptRepository.findByUserOrderByAttemptDateDescCreatedAtDesc(user);

        // Map topic to latest attempt date
        Map<String, LocalDate> topicLastPracticed = new HashMap<>();
        for (DSAAttempt attempt : allAttempts) {
            String primary = attempt.getProblem().getPrimaryTopic();
            if (primary != null) {
                topicLastPracticed.putIfAbsent(primary, attempt.getAttemptDate());
            }
            if (attempt.getProblem().getAdditionalTopics() != null) {
                for (String t : attempt.getProblem().getAdditionalTopics()) {
                    topicLastPracticed.putIfAbsent(t, attempt.getAttemptDate());
                }
            }
        }

        List<DSATopicProgressResponse> results = new ArrayList<>();

        for (String topic : DSATopicConstants.STANDARD_TOPICS) {
            long total = 0;
            long solved = 0;
            long attempted = 0;

            for (DSAProblem p : allProblems) {
                boolean matches = topic.equalsIgnoreCase(p.getPrimaryTopic()) ||
                        (p.getAdditionalTopics() != null && p.getAdditionalTopics().stream().anyMatch(t -> t.equalsIgnoreCase(topic)));
                if (matches) {
                    total++;
                    if (p.getStatus() == DSAStatus.SOLVED) {
                        solved++;
                    } else if (p.getStatus() == DSAStatus.ATTEMPTED) {
                        attempted++;
                    }
                }
            }

            long notStarted = Math.max(0, total - solved - attempted);
            double completionRate = total > 0 ? Math.round(((double) solved / total) * 1000.0) / 10.0 : 0.0;
            LocalDate lastPractice = topicLastPracticed.get(topic);

            results.add(new DSATopicProgressResponse(
                    topic,
                    total,
                    solved,
                    attempted,
                    notStarted,
                    completionRate,
                    lastPractice
            ));
        }

        return results;
    }

    @Transactional(readOnly = true)
    public DSADifficultyAnalyticsResponse getDifficultyAnalytics(User user) {
        long easySolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.EASY, DSAStatus.SOLVED);
        long easyAttempted = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.EASY, DSAStatus.ATTEMPTED);
        long easyTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.EASY);
        long easyNotStarted = Math.max(0, easyTotal - easySolved - easyAttempted);
        double easyRate = easyTotal > 0 ? Math.round(((double) easySolved / easyTotal) * 1000.0) / 10.0 : 0.0;

        long medSolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.MEDIUM, DSAStatus.SOLVED);
        long medAttempted = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.MEDIUM, DSAStatus.ATTEMPTED);
        long medTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.MEDIUM);
        long medNotStarted = Math.max(0, medTotal - medSolved - medAttempted);
        double medRate = medTotal > 0 ? Math.round(((double) medSolved / medTotal) * 1000.0) / 10.0 : 0.0;

        long hardSolved = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.HARD, DSAStatus.SOLVED);
        long hardAttempted = problemRepository.countByUserAndDifficultyAndStatus(user, DSADifficulty.HARD, DSAStatus.ATTEMPTED);
        long hardTotal = problemRepository.countByUserAndDifficulty(user, DSADifficulty.HARD);
        long hardNotStarted = Math.max(0, hardTotal - hardSolved - hardAttempted);
        double hardRate = hardTotal > 0 ? Math.round(((double) hardSolved / hardTotal) * 1000.0) / 10.0 : 0.0;

        return new DSADifficultyAnalyticsResponse(
                new DSADifficultyAnalyticsResponse.DifficultyMetric(easyTotal, easySolved, easyAttempted, easyNotStarted, easyRate),
                new DSADifficultyAnalyticsResponse.DifficultyMetric(medTotal, medSolved, medAttempted, medNotStarted, medRate),
                new DSADifficultyAnalyticsResponse.DifficultyMetric(hardTotal, hardSolved, hardAttempted, hardNotStarted, hardRate)
        );
    }

    @Transactional(readOnly = true)
    public List<DSAActivityTimelineItem> getActivityTimeline(User user, int days) {
        int windowDays = (days > 0 && days <= 90) ? days : 14;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(windowDays - 1);

        List<DSAAttempt> attempts = attemptRepository.findByUserAndAttemptDateBetweenOrderByAttemptDateAsc(
                user, startDate, endDate);

        Map<LocalDate, long[]> dayStats = new LinkedHashMap<>();
        for (int i = 0; i < windowDays; i++) {
            dayStats.put(startDate.plusDays(i), new long[]{0, 0, 0}); // [total, success, failed]
        }

        for (DSAAttempt a : attempts) {
            LocalDate d = a.getAttemptDate();
            if (dayStats.containsKey(d)) {
                long[] counts = dayStats.get(d);
                counts[0]++;
                if (a.getOutcome() == DSAAttemptOutcome.SUCCESS) {
                    counts[1]++;
                } else {
                    counts[2]++;
                }
            }
        }

        List<DSAActivityTimelineItem> items = new ArrayList<>();
        dayStats.forEach((date, counts) -> items.add(new DSAActivityTimelineItem(date, counts[0], counts[1], counts[2])));
        return items;
    }

    private int calculateCurrentStreak(User user) {
        List<LocalDate> dates = attemptRepository.findDistinctAttemptDatesByUser(user);
        if (dates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate latest = dates.get(0);

        // If latest attempt is older than yesterday, streak is broken
        long daysFromToday = ChronoUnit.DAYS.between(latest, today);
        if (daysFromToday > 1) {
            return 0;
        }

        int streak = 1;
        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate current = dates.get(i);
            LocalDate prev = dates.get(i + 1);
            if (ChronoUnit.DAYS.between(prev, current) == 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private int calculateLongestStreak(User user) {
        List<LocalDate> dates = attemptRepository.findDistinctAttemptDatesByUser(user);
        if (dates.isEmpty()) {
            return 0;
        }

        int maxStreak = 1;
        int currentStreak = 1;

        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate current = dates.get(i);
            LocalDate prev = dates.get(i + 1);
            if (ChronoUnit.DAYS.between(prev, current) == 1) {
                currentStreak++;
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak;
                }
            } else {
                currentStreak = 1;
            }
        }
        return maxStreak;
    }

    private List<String> generateInsights(
            User user, long total, long solved, long easyTotal, long easySolved,
            long medTotal, long medSolved, long hardTotal, long hardSolved,
            int streak, long overdueRevisions
    ) {
        List<String> insights = new ArrayList<>();

        if (total == 0) {
            insights.add("Welcome to the DSA Progress Tracker! Add your first problem to start building your mastery portfolio.");
            return insights;
        }

        if (overdueRevisions > 0) {
            insights.add(String.format("You have %d problem%s overdue for revision. Regular spaced review improves retention by up to 80%%.",
                    overdueRevisions, overdueRevisions > 1 ? "s" : ""));
        }

        if (streak >= 3) {
            insights.add(String.format("Excellent consistency! You are on an active %d-day practice streak.", streak));
        } else if (streak == 0 && total > 0) {
            insights.add("No practice attempts recorded today. Solve or attempt a problem to kickstart your practice streak!");
        }

        // Difficulty insight
        if (easyTotal > 0 && ((double) easySolved / easyTotal) >= 0.8 && medTotal == 0) {
            insights.add("Great mastery of Easy problems! Consider challenging yourself with Medium-difficulty questions.");
        } else if (medTotal > 0 && ((double) medSolved / medTotal) < 0.4) {
            insights.add("Medium problems have a lower completion rate. Breaking down patterns into two-pointer or sliding-window templates can help.");
        }

        // Compare topic completion
        List<DSATopicProgressResponse> topics = getTopicProgressList(user);
        var activeTopics = topics.stream().filter(t -> t.totalProblems() > 0).toList();
        if (activeTopics.size() >= 2) {
            var sorted = activeTopics.stream()
                    .sorted(Comparator.comparingDouble(DSATopicProgressResponse::completionPercentage))
                    .toList();
            var lowest = sorted.get(0);
            var highest = sorted.get(sorted.size() - 1);

            if (highest.completionPercentage() - lowest.completionPercentage() >= 25.0) {
                insights.add(String.format("Your %s completion (%.0f%%) is lower than %s (%.0f%%). Consider revisiting %s patterns.",
                        lowest.topicName(), lowest.completionPercentage(),
                        highest.topicName(), highest.completionPercentage(),
                        lowest.topicName()));
            }
        }

        if (insights.isEmpty()) {
            insights.add("Keep up the steady progress. Focus on writing clean complexity analysis and recording post-solution takeaways.");
        }

        return insights;
    }

    /**
     * Clean contextual summary for future AI Chat / RAG integration (Milestone 5 ready).
     */
    @Transactional(readOnly = true)
    public String getDsaSummaryForAiContext(User user) {
        long total = problemRepository.countByUser(user);
        if (total == 0) {
            return "DSA Profile: No coding problems tracked yet.";
        }
        long solved = problemRepository.countByUserAndStatus(user, DSAStatus.SOLVED);
        int streak = calculateCurrentStreak(user);

        return String.format(
                "DSA Profile: %d problems tracked (%d solved, %d remaining). Current practice streak: %d days.",
                total, solved, total - solved, streak
        );
    }
}

