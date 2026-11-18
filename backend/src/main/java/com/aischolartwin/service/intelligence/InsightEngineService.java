package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.InsightDtos;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.intelligence.InsightRepository;
import com.aischolartwin.repository.learning.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class InsightEngineService {

    private static final Logger log = LoggerFactory.getLogger(InsightEngineService.class);

    private final InsightRepository insightRepository;
    private final LearningProfileRepository profileRepository;
    private final LearningTaskRepository taskRepository;
    private final SpacedRevisionItemRepository revisionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final DSAProblemRepository dsaProblemRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;

    public InsightEngineService(InsightRepository insightRepository,
                                LearningProfileRepository profileRepository,
                                LearningTaskRepository taskRepository,
                                SpacedRevisionItemRepository revisionRepository,
                                AssessmentAttemptRepository attemptRepository,
                                CareerSkillRepository careerSkillRepository,
                                DSAProblemRepository dsaProblemRepository,
                                KnowledgeItemRepository knowledgeItemRepository) {
        this.insightRepository = insightRepository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
        this.revisionRepository = revisionRepository;
        this.attemptRepository = attemptRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.dsaProblemRepository = dsaProblemRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
    }

    @Transactional(readOnly = true)
    public List<InsightDtos.InsightResponse> getInsights(User user, InsightCategory category, InsightStatus status) {
        InsightStatus targetStatus = status != null ? status : InsightStatus.ACTIVE;
        List<Insight> insights = (category != null)
                ? insightRepository.findByUserAndCategoryAndStatusOrderByGeneratedAtDesc(user, category, targetStatus)
                : insightRepository.findByUserAndStatusOrderByGeneratedAtDesc(user, targetStatus);

        return insights.stream().map(InsightDtos.InsightResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public InsightDtos.InsightResponse getInsight(User user, Long id) {
        Insight insight = insightRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Insight not found with ID: " + id));
        return InsightDtos.InsightResponse.fromEntity(insight);
    }

    @Transactional
    public InsightDtos.GenerateInsightsResponse generateInsights(User user, boolean forceRefresh) {
        if (!forceRefresh) {
            List<Insight> existing = insightRepository.findByUserAndStatusOrderByGeneratedAtDesc(user, InsightStatus.ACTIVE);
            if (!existing.isEmpty()) {
                return new InsightDtos.GenerateInsightsResponse(
                        existing.size(),
                        existing.get(0).getGeneratedAt(),
                        existing.stream().map(InsightDtos.InsightResponse::fromEntity).toList()
                );
            }
        }

        List<Insight> generated = new ArrayList<>();

        // 1. Learning Insights: Profile weak concepts & assessment scores
        profileRepository.findByUser(user).ifPresent(p -> {
            if (p.getWeakAreas() != null && !p.getWeakAreas().isBlank()) {
                Insight in = new Insight(
                        user,
                        "Diagnosed Core Knowledge Gaps in Curriculum",
                        "Your academic profile records specific weak areas that directly correlate with lower assessment diagnostic confidence.",
                        InsightCategory.LEARNING,
                        InsightImportance.HIGH,
                        0.90,
                        "Schedule prioritized active-recall revision cards in Spaced Revision Engine.",
                        "LEARNING_HUB",
                        "{\"tab\": \"spaced-revision\"}"
                );
                in.addEvidence(new InsightEvidence(in, "DIAGNOSED_WEAKNESS", p.getWeakAreas(), "LearningProfile", "Profile Weakness List"));
                generated.add(in);
            }
        });

        // 2. DSA Insights: Problem solving distribution
        int solvedDsa = dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user).size();
        Insight dsaInsight = new Insight(
                user,
                "Algorithmic Pattern Velocity & Interview Readiness",
                "You have logged " + solvedDsa + " problems on your tracker. Tier-1 engineering interviews heavily test tree recursion, sliding windows, and dynamic programming.",
                InsightCategory.DSA,
                solvedDsa < 20 ? InsightImportance.HIGH : InsightImportance.MEDIUM,
                0.85,
                "Target 2 medium difficulty problems in graph traversal and dynamic programming this week.",
                "DSA_TRACKER",
                "{\"filter\": \"MEDIUM\"}"
        );
        dsaInsight.addEvidence(new InsightEvidence(dsaInsight, "DSA_SOLVED_COUNT", String.valueOf(solvedDsa), "DSAProblemRepository", solvedDsa + " Problems"));
        generated.add(dsaInsight);

        // 3. Career Insights: Skills vs Target Role
        List<CareerSkill> skills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        String targetRole = profileRepository.findByUser(user).map(LearningProfile::getTargetRole).orElse("Software Development Engineer");
        Insight careerInsight = new Insight(
                user,
                "Role Skill Coverage for " + targetRole,
                "You have " + skills.size() + " verified career technical skills. Top industry benchmarks expect verified proficiency in distributed caching, database indexing, and CI/CD pipelines.",
                InsightCategory.CAREER,
                skills.size() < 5 ? InsightImportance.HIGH : InsightImportance.MEDIUM,
                0.88,
                "Run an ATS Resume Diagnostic scan and add missing skills to your Career Hub.",
                "CAREER_HUB",
                "{\"tab\": \"skills\"}"
        );
        careerInsight.addEvidence(new InsightEvidence(careerInsight, "VERIFIED_SKILLS_COUNT", String.valueOf(skills.size()), "CareerSkillRepository", skills.size() + " Verified Skills"));
        generated.add(careerInsight);

        // 4. Knowledge Insights: Vault Notes
        int noteCount = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user).size();
        Insight knowledgeInsight = new Insight(
                user,
                "Academic Vault Knowledge Density",
                "Your Knowledge Hub currently holds " + noteCount + " structured notes. Connecting these notes to active roadmap tasks boosts long-term comprehension by 40%.",
                InsightCategory.KNOWLEDGE,
                InsightImportance.LOW,
                0.80,
                "Link your primary course notes to current roadmap milestones.",
                "KNOWLEDGE_HUB",
                "{\"tab\": \"vault\"}"
        );
        knowledgeInsight.addEvidence(new InsightEvidence(knowledgeInsight, "VAULT_NOTES_COUNT", String.valueOf(noteCount), "KnowledgeItemRepository", noteCount + " Items"));
        generated.add(knowledgeInsight);

        // 5. Productivity Insights: Study streak & active tasks
        long completedTasks = taskRepository.findByUserAndStatus(user, TaskStatus.COMPLETED).size();
        Insight prodInsight = new Insight(
                user,
                "Milestone Execution & Focus Consistency",
                "You have completed " + completedTasks + " structured learning tasks across your enrolled curricula.",
                InsightCategory.PRODUCTIVITY,
                completedTasks > 3 ? InsightImportance.MEDIUM : InsightImportance.HIGH,
                0.85,
                "Review today's Daily Study Planner to maintain study velocity.",
                "LEARNING_HUB",
                "{\"tab\": \"planner\"}"
        );
        prodInsight.addEvidence(new InsightEvidence(prodInsight, "COMPLETED_TASKS_COUNT", String.valueOf(completedTasks), "LearningTaskRepository", completedTasks + " Completed Tasks"));
        generated.add(prodInsight);

        List<Insight> saved = insightRepository.saveAll(generated);
        log.info("Generated and saved {} personalized insights for user: {}", saved.size(), user.getEmail());

        return new InsightDtos.GenerateInsightsResponse(
                saved.size(),
                Instant.now(),
                saved.stream().map(InsightDtos.InsightResponse::fromEntity).toList()
        );
    }

    @Transactional
    public InsightDtos.InsightResponse dismissInsight(User user, Long id) {
        Insight insight = insightRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Insight not found with ID: " + id));

        insight.setStatus(InsightStatus.DISMISSED);
        Insight saved = insightRepository.save(insight);
        return InsightDtos.InsightResponse.fromEntity(saved);
    }
}
