package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.IntelligenceAnalyticsDtos.*;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.DSAAttemptOutcome;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.AIInteractionLog;
import com.aischolartwin.entity.intelligence.MemoryStatus;
import com.aischolartwin.entity.intelligence.RecommendationStatus;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.*;
import com.aischolartwin.repository.intelligence.*;
import com.aischolartwin.repository.learning.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntelligenceAnalyticsService {

    private final StudySessionRepository sessionRepository;
    private final LearningTaskRepository taskRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final SpacedRevisionItemRepository revisionRepository;
    private final RevisionAttemptRepository revisionAttemptRepository;

    private final DSAProblemRepository dsaProblemRepository;
    private final DSAAttemptRepository dsaAttemptRepository;

    private final CareerSkillRepository careerSkillRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final LearningProfileRepository profileRepository;

    private final AIInteractionLogRepository interactionLogRepository;
    private final UserMemoryRepository memoryRepository;
    private final InsightRepository insightRepository;
    private final AIRecommendationRepository recommendationRepository;

    public IntelligenceAnalyticsService(StudySessionRepository sessionRepository,
                                       LearningTaskRepository taskRepository,
                                       LearningRoadmapRepository roadmapRepository,
                                       AssessmentAttemptRepository attemptRepository,
                                       SpacedRevisionItemRepository revisionRepository,
                                       RevisionAttemptRepository revisionAttemptRepository,
                                       DSAProblemRepository dsaProblemRepository,
                                       DSAAttemptRepository dsaAttemptRepository,
                                       CareerSkillRepository careerSkillRepository,
                                       ResumeAnalysisRepository resumeAnalysisRepository,
                                       LearningProfileRepository profileRepository,
                                       AIInteractionLogRepository interactionLogRepository,
                                       UserMemoryRepository memoryRepository,
                                       InsightRepository insightRepository,
                                       AIRecommendationRepository recommendationRepository) {
        this.sessionRepository = sessionRepository;
        this.taskRepository = taskRepository;
        this.roadmapRepository = roadmapRepository;
        this.attemptRepository = attemptRepository;
        this.revisionRepository = revisionRepository;
        this.revisionAttemptRepository = revisionAttemptRepository;
        this.dsaProblemRepository = dsaProblemRepository;
        this.dsaAttemptRepository = dsaAttemptRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.profileRepository = profileRepository;
        this.interactionLogRepository = interactionLogRepository;
        this.memoryRepository = memoryRepository;
        this.insightRepository = insightRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public LearningIntelligenceDto getLearningIntelligence(User user) {
        List<StudySession> sessions = sessionRepository.findByUserOrderBySessionDateDescCreatedAtDesc(user);
        int sessionMinutes = sessions.stream().mapToInt(StudySession::getDurationMinutes).sum();

        List<LearningTask> completedTasks = taskRepository.findByUserAndStatus(user, TaskStatus.COMPLETED);
        List<LearningTask> allTasks = taskRepository.findByUser(user);
        int taskMinutes = completedTasks.stream().mapToInt(t -> t.getActualMinutes() > 0 ? t.getActualMinutes() : t.getEstimatedMinutes()).sum();
        double totalHours = Math.round(((sessionMinutes + taskMinutes) / 60.0) * 10.0) / 10.0;

        int taskCompletionRate = allTasks.isEmpty() ? 0 : (int) Math.round(((double) completedTasks.size() / allTasks.size()) * 100);

        List<LearningRoadmap> roadmaps = roadmapRepository.findByUserOrderByCreatedAtDesc(user);
        int avgRoadmapProgress = 0;
        if (!roadmaps.isEmpty()) {
            int sumPct = roadmaps.stream().mapToInt(LearningRoadmap::getCompletionPercentage).sum();
            avgRoadmapProgress = (int) Math.round((double) sumPct / roadmaps.size());
        }

        List<AssessmentAttempt> attempts = attemptRepository.findByUserOrderByCompletedAtDesc(user);
        double avgScore = 0.0;
        if (!attempts.isEmpty()) {
            double totalScore = attempts.stream().mapToInt(AssessmentAttempt::getScorePercentage).sum();
            avgScore = Math.round((totalScore / attempts.size()) * 10.0) / 10.0;
        }

        List<RevisionAttempt> revAttempts = revisionAttemptRepository.findByUserOrderByAttemptedAtDesc(user);
        double retentionRate = 0.0;
        if (!revAttempts.isEmpty()) {
            long goodRecalls = revAttempts.stream()
                    .filter(a -> a.getRating() == RevisionRating.EASY || a.getRating() == RevisionRating.MODERATE)
                    .count();
            retentionRate = Math.round(((double) goodRecalls / revAttempts.size()) * 1000.0) / 10.0;
        }

        int streakDays = calculateStudyStreak(sessions);

        return new LearningIntelligenceDto(
                totalHours,
                completedTasks.size(),
                completedTasks.size(),
                sessions.size(),
                streakDays,
                avgRoadmapProgress,
                taskCompletionRate,
                avgScore,
                retentionRate,
                retentionRate
        );
    }

    @Transactional(readOnly = true)
    public DSAIntelligenceDto getDsaIntelligence(User user) {
        List<DSAProblem> problems = dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user);
        int totalSolved = 0;
        int easySolved = 0;
        int mediumSolved = 0;
        int hardSolved = 0;
        Map<String, Integer> topicSolvedCounts = new HashMap<>();
        Map<String, Integer> topicTotalCounts = new HashMap<>();

        for (DSAProblem p : problems) {
            String topic = p.getPrimaryTopic() != null ? p.getPrimaryTopic() : "General DSA";
            topicTotalCounts.merge(topic, 1, Integer::sum);

            boolean isSolved = p.getStatus() == DSAStatus.SOLVED;
            if (isSolved) {
                totalSolved++;
                String diff = p.getDifficulty() != null ? p.getDifficulty().name().toUpperCase() : "MEDIUM";
                if (diff.contains("EASY")) easySolved++;
                else if (diff.contains("HARD")) hardSolved++;
                else mediumSolved++;

                topicSolvedCounts.merge(topic, 1, Integer::sum);
            }
        }

        String topTopic = topicSolvedCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(problems.isEmpty() ? "None recorded" : problems.get(0).getPrimaryTopic());

        // Derive weakest topic based on uncompleted or attempted problems
        String weakestTopic = problems.stream()
                .filter(p -> p.getStatus() != DSAStatus.SOLVED || p.isRevisionRequired())
                .map(p -> p.getPrimaryTopic() != null ? p.getPrimaryTopic() : "General DSA")
                .findFirst()
                .orElse("None identified");

        // Accuracy from actual DSA attempts if present, otherwise solve rate from problem records
        long totalAttempts = dsaAttemptRepository.countByUser(user);
        double accuracy = 0.0;
        if (totalAttempts > 0) {
            long successfulAttempts = dsaAttemptRepository.countByUserAndOutcome(user, DSAAttemptOutcome.SUCCESS);
            accuracy = Math.round(((double) successfulAttempts / totalAttempts) * 1000.0) / 10.0;
        } else if (!problems.isEmpty()) {
            accuracy = Math.round(((double) totalSolved / problems.size()) * 1000.0) / 10.0;
        }

        double overallSolveRate = problems.isEmpty() ? 0.0 : Math.round(((double) totalSolved / problems.size()) * 1000.0) / 10.0;

        return new DSAIntelligenceDto(
                totalSolved,
                totalSolved,
                problems.size(),
                easySolved,
                easySolved,
                mediumSolved,
                mediumSolved,
                hardSolved,
                hardSolved,
                accuracy,
                overallSolveRate,
                topTopic,
                weakestTopic,
                topicTotalCounts
        );
    }

    @Transactional(readOnly = true)
    public CareerIntelligenceDto getCareerIntelligence(User user) {
        int atsScore = 0;
        boolean hasResumeAnalysis = false;
        try {
            var analyses = resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
            if (!analyses.isEmpty()) {
                atsScore = analyses.get(0).getOverallScore();
                hasResumeAnalysis = true;
            }
        } catch (Exception ignored) {}

        List<CareerSkill> skills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        int verifiedCount = skills.size();

        String targetRole = profileRepository.findByUser(user)
                .map(LearningProfile::getTargetRole)
                .orElse("Software Development Engineer");

        int targetSkillsBenchmark = 8;
        int missingCount = Math.max(0, targetSkillsBenchmark - verifiedCount);
        double skillCoverage = Math.min(100.0, Math.round(((double) verifiedCount / targetSkillsBenchmark) * 1000.0) / 10.0);

        // Deterministic readiness: 0 if no skills and no resume scanned; no fabricated 40% floor
        int readinessPct = 0;
        if (verifiedCount > 0 || hasResumeAnalysis) {
            readinessPct = Math.min(100, (int) Math.round((skillCoverage * 0.6) + (atsScore * 0.4)));
        }

        List<String> topSkillGaps = List.of();
        if (missingCount > 0) {
            Set<String> existingSkillNames = skills.stream()
                    .map(s -> s.getSkillName().toLowerCase())
                    .collect(Collectors.toSet());
            List<String> standardBenchmarks = List.of("Docker & Containers", "System Design", "Microservices", "CI/CD Pipelines", "Redis Caching", "Cloud Architecture");
            topSkillGaps = standardBenchmarks.stream()
                    .filter(b -> !existingSkillNames.contains(b.toLowerCase()))
                    .limit(missingCount)
                    .toList();
        }

        return new CareerIntelligenceDto(
                atsScore,
                readinessPct,
                readinessPct,
                verifiedCount,
                targetSkillsBenchmark,
                missingCount,
                skillCoverage,
                targetRole,
                topSkillGaps
        );
    }

    @Transactional(readOnly = true)
    public AIUsageIntelligenceDto getAiUsageIntelligence(User user) {
        long conversations = interactionLogRepository.countByUser(user);
        long activeMemories = memoryRepository.countByUserAndStatus(user, MemoryStatus.ACTIVE);
        long pinnedMemories = memoryRepository.findByUserAndIsPinnedTrueAndStatusOrderByUpdatedAtDesc(user, MemoryStatus.ACTIVE).size();
        long insightsCount = insightRepository.countByUserAndStatus(user, com.aischolartwin.entity.intelligence.InsightStatus.ACTIVE);
        long pendingRecs = recommendationRepository.countByUserAndStatus(user, RecommendationStatus.PENDING);

        List<AIInteractionLog> recentLogs = interactionLogRepository.findByUserOrderByCreatedAtDesc(user);
        List<String> topTopics = recentLogs.stream()
                .map(AIInteractionLog::getIntent)
                .filter(Objects::nonNull)
                .filter(intent -> !intent.isBlank())
                .distinct()
                .limit(5)
                .toList();

        if (topTopics.isEmpty()) {
            topTopics = memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(user, MemoryStatus.ACTIVE).stream()
                    .map(m -> m.getCategory() != null ? m.getCategory().name() : "General")
                    .distinct()
                    .limit(4)
                    .toList();
        }

        long totalQueries = recentLogs.size();
        long groundedCount = recentLogs.stream().filter(l -> l.getUsedMemoryIdsJson() != null && !l.getUsedMemoryIdsJson().isBlank() && !l.getUsedMemoryIdsJson().equals("[]")).count();
        int groundingRate = totalQueries > 0 ? (int) Math.round(((double) groundedCount / totalQueries) * 100) : 100;
        long avgLatency = (long) recentLogs.stream().mapToLong(l -> l.getLatencyMs() != null ? l.getLatencyMs() : 650L).average().orElse(650.0);

        return new AIUsageIntelligenceDto(
                conversations,
                totalQueries,
                activeMemories,
                pinnedMemories,
                insightsCount,
                pendingRecs,
                groundingRate,
                groundedCount,
                avgLatency,
                topTopics
        );
    }

    @Transactional(readOnly = true)
    public IntelligenceOverviewResponse getOverview(User user) {
        LearningIntelligenceDto learning = getLearningIntelligence(user);
        DSAIntelligenceDto dsa = getDsaIntelligence(user);
        CareerIntelligenceDto career = getCareerIntelligence(user);
        AIUsageIntelligenceDto aiUsage = getAiUsageIntelligence(user);

        // Determine data presence for each pillar
        boolean revisionHasData = revisionAttemptRepository.findByUserOrderByAttemptedAtDesc(user).size() > 0;
        boolean dsaHasData = dsa.totalAttempted() > 0 || dsaAttemptRepository.countByUser(user) > 0;
        boolean careerHasData = career.verifiedSkillsCount() > 0 || career.latestResumeAtsScore() > 0;

        double revisionScore = revisionHasData ? learning.revisionAccuracyPercentage() : 0.0;
        double dsaScore = dsaHasData ? dsa.overallSolveRate() : 0.0;
        double careerScore = careerHasData ? career.careerReadinessPercentage() : 0.0;

        // Proportional Weight Redistribution Policy:
        // Base weights: Revision 0.35, DSA 0.35, Career 0.30 (Sum = 1.00)
        double baseRevWeight = 0.35;
        double baseDsaWeight = 0.35;
        double baseCarWeight = 0.30;

        double activeWeightSum = (revisionHasData ? baseRevWeight : 0.0)
                + (dsaHasData ? baseDsaWeight : 0.0)
                + (careerHasData ? baseCarWeight : 0.0);

        double effRevWeight = activeWeightSum > 0 && revisionHasData ? (baseRevWeight / activeWeightSum) : 0.0;
        double effDsaWeight = activeWeightSum > 0 && dsaHasData ? (baseDsaWeight / activeWeightSum) : 0.0;
        double effCarWeight = activeWeightSum > 0 && careerHasData ? (baseCarWeight / activeWeightSum) : 0.0;

        double revContrib = Math.round(revisionScore * effRevWeight * 10.0) / 10.0;
        double dsaContrib = Math.round(dsaScore * effDsaWeight * 10.0) / 10.0;
        double carContrib = Math.round(careerScore * effCarWeight * 10.0) / 10.0;

        int compositeScore = activeWeightSum > 0 ? (int) Math.round(revContrib + dsaContrib + carContrib) : 0;

        List<CompositeIndexPillarDto> breakdown = List.of(
                new CompositeIndexPillarDto(
                        "Spaced Revision Accuracy",
                        revisionScore,
                        Math.round(effRevWeight * 100.0),
                        revContrib,
                        revisionHasData,
                        revisionHasData ? "Active (" + learning.revisionRetentionRate() + "% retention)" : "INSUFFICIENT_DATA (No revision reviews completed yet)"
                ),
                new CompositeIndexPillarDto(
                        "DSA Algorithmic Solve Rate",
                        dsaScore,
                        Math.round(effDsaWeight * 100.0),
                        dsaContrib,
                        dsaHasData,
                        dsaHasData ? "Active (" + dsa.totalProblemsSolved() + " problems solved)" : "INSUFFICIENT_DATA (No DSA problems logged yet)"
                ),
                new CompositeIndexPillarDto(
                        "Career Skill Coverage",
                        careerScore,
                        Math.round(effCarWeight * 100.0),
                        carContrib,
                        careerHasData,
                        careerHasData ? "Active (" + career.verifiedSkillsCount() + " skills verified, ATS: " + career.latestResumeAtsScore() + ")" : "INSUFFICIENT_DATA (No skills or resume scan logged yet)"
                )
        );

        String calculationPolicy = "Deterministic 35% Spaced Revision + 35% DSA Solve Rate + 30% Career Skill Coverage. Unassessed pillars redistribute weights proportionally across active pillars.";

        // Dynamic Strengths based strictly on database records
        List<String> strengths = new ArrayList<>();
        if (dsa.totalProblemsSolved() > 0) {
            strengths.add("Solved " + dsa.totalProblemsSolved() + " DSA problems (Top: " + dsa.topTopic() + ")");
        }
        if (revisionHasData && learning.revisionRetentionRate() > 0) {
            strengths.add("Active recall retention rate of " + learning.revisionRetentionRate() + "% across revision items");
        }
        if (career.verifiedSkillsCount() > 0) {
            strengths.add(career.verifiedSkillsCount() + " verified technical skills aligned with " + career.targetRole());
        }
        if (learning.topicsCompleted() > 0) {
            strengths.add(learning.topicsCompleted() + " curriculum learning tasks completed");
        }
        if (strengths.isEmpty()) {
            strengths.add("Begin study sessions, log DSA problems, and upload a resume to establish strength baselines.");
        }

        // Dynamic Weaknesses
        List<String> weaknesses = new ArrayList<>();
        if (!"None identified".equalsIgnoreCase(dsa.weakestTopic()) && !"None recorded".equalsIgnoreCase(dsa.weakestTopic())) {
            weaknesses.add("Practice needed on " + dsa.weakestTopic() + " (flagged in DSA Tracker)");
        }
        if (career.missingSkillsCount() > 0) {
            weaknesses.add(career.missingSkillsCount() + " target skills missing for " + career.targetRole() + " benchmark");
        }
        if (!revisionHasData) {
            weaknesses.add("No spaced repetition reviews completed yet to verify long-term memory");
        }
        if (career.latestResumeAtsScore() == 0) {
            weaknesses.add("No resume ATS scan on file in Career & Resume Hub");
        }
        if (weaknesses.isEmpty()) {
            weaknesses.add("All tracked competencies meet current target benchmark thresholds.");
        }

        // Dynamic Priority Actions
        List<String> priorityActions = new ArrayList<>();
        if (career.latestResumeAtsScore() == 0) {
            priorityActions.add("Upload your latest resume PDF in Career Hub for instant ATS diagnostic feedback");
        } else if (career.missingSkillsCount() > 0) {
            priorityActions.add("Add evidence for missing core skills in Career Hub to elevate role readiness");
        }
        if (dsa.totalProblemsSolved() == 0) {
            priorityActions.add("Solve your first algorithmic problem in DSA Tracker");
        } else if (!"None identified".equalsIgnoreCase(dsa.weakestTopic())) {
            priorityActions.add("Solve 2 medium problems in " + dsa.weakestTopic() + " to resolve flagged gaps");
        }
        if (!revisionHasData) {
            priorityActions.add("Schedule and review your first topic in Spaced Revision Engine");
        } else {
            priorityActions.add("Maintain daily revision streak with today's scheduled reviews");
        }

        return new IntelligenceOverviewResponse(
                compositeScore,
                calculationPolicy,
                breakdown,
                learning,
                dsa,
                career,
                aiUsage,
                learning,
                dsa,
                career,
                strengths,
                weaknesses,
                priorityActions
        );
    }

    private int calculateStudyStreak(List<StudySession> sessions) {
        if (sessions.isEmpty()) return 0;
        Set<LocalDate> dates = sessions.stream()
                .map(StudySession::getSessionDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        if (dates.isEmpty()) return 0;

        LocalDate current = LocalDate.now();
        // If no session today, check if yesterday was active
        if (!dates.contains(current)) {
            current = current.minusDays(1);
            if (!dates.contains(current)) {
                return 0;
            }
        }

        int streak = 0;
        while (dates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }
        return streak;
    }
}
