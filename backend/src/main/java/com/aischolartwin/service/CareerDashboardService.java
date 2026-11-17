package com.aischolartwin.service;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CareerDashboardService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository;
    private final CareerActionRepository careerActionRepository;
    private final RoleReadinessService roleReadinessService;
    private final JobDescriptionService jobDescriptionService;
    private final DSAProblemRepository dsaProblemRepository;

    public CareerDashboardService(ResumeRepository resumeRepository,
                                  ResumeAnalysisRepository resumeAnalysisRepository,
                                  CareerSkillRepository careerSkillRepository,
                                  JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository,
                                  CareerActionRepository careerActionRepository,
                                  RoleReadinessService roleReadinessService,
                                  JobDescriptionService jobDescriptionService,
                                  DSAProblemRepository dsaProblemRepository) {
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.jobDescriptionAnalysisRepository = jobDescriptionAnalysisRepository;
        this.careerActionRepository = careerActionRepository;
        this.roleReadinessService = roleReadinessService;
        this.jobDescriptionService = jobDescriptionService;
        this.dsaProblemRepository = dsaProblemRepository;
    }

    @Transactional(readOnly = true)
    public CareerDashboardSummaryResponse getDashboardSummary(User user) {
        long totalResumes = resumeRepository.countByUser(user);
        Optional<Resume> primaryOpt = resumeRepository.findByUserAndIsPrimaryTrue(user)
                .or(() -> resumeRepository.findByUserOrderByCreatedAtDesc(user).stream().findFirst());

        ResumeResponse primaryResponse = null;
        Integer latestScore = null;
        Integer previousScore = null;
        Integer scoreDiff = null;

        if (primaryOpt.isPresent()) {
            Resume primary = primaryOpt.get();
            List<ResumeAnalysis> analyses = resumeAnalysisRepository.findByResumeOrderByCreatedAtDesc(primary);
            if (!analyses.isEmpty()) {
                latestScore = analyses.get(0).getOverallScore();
                if (analyses.size() > 1) {
                    previousScore = analyses.get(1).getOverallScore();
                    scoreDiff = latestScore - previousScore;
                }
            }
            primaryResponse = ResumeResponse.fromEntity(primary, latestScore != null, latestScore);
        }

        // Skills inventory metrics
        List<CareerSkill> skills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        long totalSkillsCount = skills.size();

        Map<String, Long> skillsByCategory = new LinkedHashMap<>();
        for (CareerSkillCategory cat : CareerSkillCategory.values()) {
            skillsByCategory.put(cat.name(), 0L);
        }
        for (CareerSkill s : skills) {
            String catName = s.getCategory().name();
            skillsByCategory.put(catName, skillsByCategory.getOrDefault(catName, 0L) + 1);
        }

        List<String> strongestCategories = skillsByCategory.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> CareerSkillCategory.valueOf(e.getKey()).getDisplayName())
                .toList();

        // Target role readiness (default to SDE)
        RoleReadinessDto targetRole = roleReadinessService.evaluateRoleReadiness(user, "sde");

        // Job description matches
        long totalJdCount = jobDescriptionAnalysisRepository.countByUser(user);
        List<JobDescriptionAnalysisResponse> recentJd = jobDescriptionService.getUserJobAnalyses(user).stream()
                .limit(3)
                .toList();

        // Career action counts
        long pendingActionsCount = careerActionRepository.countByUserAndStatus(user, CareerActionStatus.NOT_STARTED)
                + careerActionRepository.countByUserAndStatus(user, CareerActionStatus.IN_PROGRESS);
        long completedActionsCount = careerActionRepository.countByUserAndStatus(user, CareerActionStatus.COMPLETED);

        List<CareerActionResponse> urgentActions = careerActionRepository
                .findByUserAndStatusOrderBySuggestedDeadlineAscCreatedAtDesc(user, CareerActionStatus.NOT_STARTED)
                .stream()
                .limit(4)
                .map(CareerActionResponse::fromEntity)
                .toList();

        // Rule-Based Dynamic Career Insights
        List<String> insights = new ArrayList<>();

        if (totalResumes == 0) {
            insights.add("Upload your PDF or DOCX resume to unlock automated rubric analysis and role-readiness benchmarks.");
        } else if (latestScore != null && latestScore < 70) {
            insights.add(String.format("Your primary resume scored %d/100. Review the dimension breakdown to identify high-leverage bullet improvements.", latestScore));
        } else if (latestScore != null && latestScore >= 80) {
            insights.add(String.format("Great progress! Your resume scored %d/100 with solid technical breadth.", latestScore));
        }

        if (scoreDiff != null && scoreDiff > 0) {
            insights.add(String.format("Your latest resume iteration improved by +%d points over the prior version.", scoreDiff));
        }

        if (!targetRole.missingRequiredSkills().isEmpty()) {
            insights.add(String.format("Software Engineer gap alert: Prioritize verifying '%s' to boost your role readiness from %.1f%%.",
                    targetRole.missingRequiredSkills().get(0), targetRole.readinessPercentage()));
        }

        long solvedDsa = dsaProblemRepository.countByUserAndStatus(user, DSAStatus.SOLVED);
        if (solvedDsa > 0) {
            insights.add(String.format("Verified %d solved coding problems in your DSA Tracker, backing your technical profile with practical algorithmic evidence.", solvedDsa));
        }

        if (urgentActions.stream().anyMatch(CareerActionResponse::isOverdue)) {
            insights.add("You have overdue career action items. Check your action plan to stay on target for upcoming applications.");
        }

        return new CareerDashboardSummaryResponse(
                totalResumes,
                primaryResponse,
                latestScore,
                previousScore,
                scoreDiff,
                totalSkillsCount,
                skillsByCategory,
                strongestCategories,
                targetRole,
                totalJdCount,
                recentJd,
                pendingActionsCount,
                completedActionsCount,
                urgentActions,
                insights
        );
    }
}

