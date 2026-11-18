package com.aischolartwin.service.learning;

import com.aischolartwin.dto.RoleReadinessDto;
import com.aischolartwin.dto.learning.LearningGapReportDto;
import com.aischolartwin.entity.*;
import com.aischolartwin.entity.learning.AssessmentAttempt;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.entity.learning.RevisionStatus;
import com.aischolartwin.entity.learning.SpacedRevisionItem;
import com.aischolartwin.repository.*;
import com.aischolartwin.repository.learning.AssessmentAttemptRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import com.aischolartwin.service.RoleReadinessService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LearningGapAnalysisService {

    private final LearningProfileService profileService;
    private final RoleReadinessService roleReadinessService;
    private final CareerSkillRepository careerSkillRepository;
    private final DSAProblemRepository dsaProblemRepository;
    private final DSARevisionRepository dsaRevisionRepository;
    private final SpacedRevisionItemRepository spacedRevisionItemRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final DocumentRepository documentRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ObjectMapper objectMapper;

    // Prerequisite Knowledge Graph
    private static final Map<String, List<String>> PREREQUISITE_MAP = Map.ofEntries(
            Map.entry("Dynamic Programming", List.of("Recursion & Backtracking", "Arrays & Hashing")),
            Map.entry("Graph Algorithms", List.of("Trees & Binary Trees", "Queue & BFS", "Stack & DFS")),
            Map.entry("Trees & Binary Search Trees", List.of("Recursion", "Linked Lists")),
            Map.entry("Two Pointers", List.of("Arrays & Strings")),
            Map.entry("Sliding Window", List.of("Arrays & Strings", "Two Pointers")),
            Map.entry("Spring Boot", List.of("Java Core", "Object-Oriented Programming", "SQL & Relational DBs")),
            Map.entry("Microservices", List.of("Spring Boot", "REST APIs", "Docker & Containers")),
            Map.entry("System Design", List.of("SQL & Database Indexing", "Caching & Redis", "Networking & HTTP")),
            Map.entry("React", List.of("JavaScript ES6+", "HTML5 & CSS3")),
            Map.entry("Docker & DevOps", List.of("Linux Fundamentals", "Networking Basics")),
            Map.entry("Distributed Systems", List.of("Operating Systems", "Computer Networks", "Concurrency & Multithreading")),
            Map.entry("Machine Learning", List.of("Python Fundamentals", "Linear Algebra", "Calculus & Probability"))
    );

    public LearningGapAnalysisService(
            LearningProfileService profileService,
            RoleReadinessService roleReadinessService,
            CareerSkillRepository careerSkillRepository,
            DSAProblemRepository dsaProblemRepository,
            DSARevisionRepository dsaRevisionRepository,
            SpacedRevisionItemRepository spacedRevisionItemRepository,
            AssessmentAttemptRepository assessmentAttemptRepository,
            DocumentRepository documentRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            ObjectMapper objectMapper
    ) {
        this.profileService = profileService;
        this.roleReadinessService = roleReadinessService;
        this.careerSkillRepository = careerSkillRepository;
        this.dsaProblemRepository = dsaProblemRepository;
        this.dsaRevisionRepository = dsaRevisionRepository;
        this.spacedRevisionItemRepository = spacedRevisionItemRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.documentRepository = documentRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public LearningGapReportDto.Response analyzeGaps(User user) {
        LearningProfile profile = profileService.getOrCreateProfileEntity(user);
        String targetRole = profile.getTargetRole() != null ? profile.getTargetRole() : "Software Development Engineer";

        // Map role name to roleId
        String roleId = mapRoleNameToId(targetRole);
        RoleReadinessDto roleReadiness = roleReadinessService.evaluateRoleReadiness(user, roleId);

        List<CareerSkill> userSkills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        List<DSAProblem> dsaProblems = dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user);
        List<DSARevision> dsaRevisions = dsaRevisionRepository.findByUserAndStatusOrderByScheduledDateAsc(user, DSARevisionStatus.PENDING);
        List<SpacedRevisionItem> spacedRevisions = spacedRevisionItemRepository.findByUserOrderByScheduledDateAsc(user);
        List<AssessmentAttempt> assessmentAttempts = assessmentAttemptRepository.findByUserOrderByCompletedAtDesc(user);
        long docCount = documentRepository.countByUser(user);
        long noteCount = knowledgeItemRepository.countByUser(user);

        // 1. Evaluate Strong Topics
        List<LearningGapReportDto.StrengthItem> strongTopics = new ArrayList<>();
        Set<String> strongTopicNames = new HashSet<>();

        // From Career Skills with ADVANCED or EXPERT
        for (CareerSkill skill : userSkills) {
            if (skill.getProficiency() == SkillProficiency.EXPERT || skill.getProficiency() == SkillProficiency.ADVANCED) {
                strongTopics.add(new LearningGapReportDto.StrengthItem(
                        skill.getSkillName(),
                        skill.getCategory().name(),
                        skill.getProficiency().name(),
                        skill.getEvidenceText() != null ? skill.getEvidenceText() : "Self-verified proficiency in Skills Inventory",
                        "CAREER_SKILLS"
                ));
                strongTopicNames.add(skill.getSkillName().toLowerCase());
            }
        }

        // From DSA Mastery
        Map<String, Integer> dsaTopicSolved = new HashMap<>();
        Map<String, Integer> dsaTopicTotal = new HashMap<>();
        for (DSAProblem p : dsaProblems) {
            String topic = p.getPrimaryTopic() != null ? p.getPrimaryTopic() : "General DSA";
            dsaTopicTotal.put(topic, dsaTopicTotal.getOrDefault(topic, 0) + 1);
            if (p.getStatus() == DSAStatus.SOLVED) {
                dsaTopicSolved.put(topic, dsaTopicSolved.getOrDefault(topic, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> entry : dsaTopicSolved.entrySet()) {
            int total = dsaTopicTotal.getOrDefault(entry.getKey(), 1);
            if (entry.getValue() >= 3 && ((double) entry.getValue() / total) >= 0.75) {
                if (!strongTopicNames.contains(entry.getKey().toLowerCase())) {
                    strongTopics.add(new LearningGapReportDto.StrengthItem(
                            entry.getKey(),
                            "ALGORITHMS_DSA",
                            "INTERMEDIATE_PLUS",
                            entry.getValue() + " problems solved with " + Math.round(((double) entry.getValue() / total) * 100) + "% solve rate",
                            "DSA_TRACKER"
                    ));
                    strongTopicNames.add(entry.getKey().toLowerCase());
                }
            }
        }

        // From Assessments
        for (AssessmentAttempt attempt : assessmentAttempts) {
            if (attempt.getScorePercentage() != null && attempt.getScorePercentage() >= 85) {
                String topic = attempt.getAssessment() != null ? attempt.getAssessment().getTopic() : "General";
                if (!strongTopicNames.contains(topic.toLowerCase())) {
                    strongTopics.add(new LearningGapReportDto.StrengthItem(
                            topic,
                            "ACADEMIC_ASSESSMENT",
                            "ASSESSED_STRONG",
                            "Scored " + attempt.getScorePercentage() + "% in verified topic assessment",
                            "ASSESSMENTS"
                    ));
                    strongTopicNames.add(topic.toLowerCase());
                }
            }
        }

        // 2. Evaluate Weak Topics / Gaps
        List<LearningGapReportDto.GapItem> weakTopics = new ArrayList<>();
        Set<String> weakTopicNames = new HashSet<>();

        // Role readiness missing required skills
        List<String> missingSkills = roleReadiness.missingRequiredSkills() != null
                ? roleReadiness.missingRequiredSkills()
                : List.of();

        for (String missing : missingSkills) {
            weakTopics.add(new LearningGapReportDto.GapItem(
                    missing,
                    "CORE_ROLE_COMPETENCY",
                    "NOT_ACQUIRED",
                    "HIGH",
                    "Mandatory core requirement for target role benchmark (" + targetRole + ")",
                    "CAREER_READINESS"
            ));
            weakTopicNames.add(missing.toLowerCase());
        }

        // Weak topics from DSA (problems with status ATTEMPTED or low solve rate or revision required)
        for (DSAProblem p : dsaProblems) {
            if (p.isRevisionRequired() || p.getStatus() == DSAStatus.ATTEMPTED) {
                String topic = p.getPrimaryTopic() != null ? p.getPrimaryTopic() : "General DSA";
                if (!weakTopicNames.contains(topic.toLowerCase()) && !strongTopicNames.contains(topic.toLowerCase())) {
                    weakTopics.add(new LearningGapReportDto.GapItem(
                            topic,
                            "ALGORITHMS_DSA",
                            "NEEDS_PRACTICE",
                            "MEDIUM",
                            "Unresolved or flagged problems in DSA Tracker: '" + p.getTitle() + "'",
                            "DSA_TRACKER"
                    ));
                    weakTopicNames.add(topic.toLowerCase());
                }
            }
        }

        // Weak areas from Learning Profile explicit inputs
        List<String> profileWeakAreas = profileService.fromJsonList(profile.getWeakAreas());
        for (String weak : profileWeakAreas) {
            if (!weakTopicNames.contains(weak.toLowerCase()) && !strongTopicNames.contains(weak.toLowerCase())) {
                weakTopics.add(new LearningGapReportDto.GapItem(
                        weak,
                        "STUDENT_SELF_REPORTED",
                        "BEGINNER",
                        "HIGH",
                        "Identified by student as personal weak area in Learning Profile",
                        "LEARNING_PROFILE"
                ));
                weakTopicNames.add(weak.toLowerCase());
            }
        }

        // Weak areas from Assessment Attempts (< 60%)
        for (AssessmentAttempt attempt : assessmentAttempts) {
            if (attempt.getScorePercentage() != null && attempt.getScorePercentage() < 60) {
                String topic = attempt.getAssessment() != null ? attempt.getAssessment().getTopic() : "General";
                if (!weakTopicNames.contains(topic.toLowerCase())) {
                    weakTopics.add(new LearningGapReportDto.GapItem(
                            topic,
                            "ACADEMIC_ASSESSMENT",
                            "NEEDS_RELEARNING",
                            "HIGH",
                            "Low score (" + attempt.getScorePercentage() + "%) in recent assessment on " + attempt.getCompletedAt(),
                            "ASSESSMENTS"
                    ));
                    weakTopicNames.add(topic.toLowerCase());
                }
            }
        }

        // 3. Topics Requiring Revision (Deduplicated across Spaced Repetition and DSA Revision Queue)
        List<LearningGapReportDto.RevisionNeedItem> revisionNeeds = new ArrayList<>();
        Set<String> seenRevisionTitles = new HashSet<>();
        LocalDate today = LocalDate.now();

        for (SpacedRevisionItem item : spacedRevisions) {
            if (item.getStatus() == RevisionStatus.DUE || item.getStatus() == RevisionStatus.OVERDUE || item.getStatus() == RevisionStatus.NEEDS_RELEARNING) {
                int daysOverdue = (int) ChronoUnit.DAYS.between(item.getScheduledDate(), today);
                String title = item.getTitle();
                seenRevisionTitles.add(title.toLowerCase());
                if (title.toLowerCase().startsWith("dsa: ")) {
                    seenRevisionTitles.add(title.substring(5).trim().toLowerCase());
                }
                revisionNeeds.add(new LearningGapReportDto.RevisionNeedItem(
                        title,
                        Math.max(0, daysOverdue),
                        item.getStatus() == RevisionStatus.NEEDS_RELEARNING ? "CRITICAL" : (daysOverdue > 3 ? "HIGH" : "MEDIUM"),
                        "Spaced repetition interval due for review (Stage " + item.getStage() + ")"
                ));
            }
        }

        for (DSARevision dRev : dsaRevisions) {
            if (dRev.getScheduledDate().isBefore(today) || dRev.getScheduledDate().isEqual(today)) {
                int days = (int) ChronoUnit.DAYS.between(dRev.getScheduledDate(), today);
                String pTitle = dRev.getProblem() != null ? dRev.getProblem().getTitle() : "DSA Problem";
                String normalizedPTitle = pTitle.toLowerCase();

                // Prevent duplicating items already present in Spaced Revision queue
                if (!seenRevisionTitles.contains(normalizedPTitle) && !seenRevisionTitles.contains("dsa: " + normalizedPTitle)) {
                    seenRevisionTitles.add(normalizedPTitle);
                    revisionNeeds.add(new LearningGapReportDto.RevisionNeedItem(
                            "DSA: " + pTitle,
                            Math.max(0, days),
                            days > 2 ? "HIGH" : "MEDIUM",
                            "Algorithmic problem revision scheduled in DSA Tracker"
                    ));
                }
            }
        }

        // 4. Prerequisite Topics Identification
        Set<String> prerequisiteSet = new LinkedHashSet<>();
        for (LearningGapReportDto.GapItem gap : weakTopics) {
            for (Map.Entry<String, List<String>> entry : PREREQUISITE_MAP.entrySet()) {
                if (gap.topic().equalsIgnoreCase(entry.getKey())) {
                    for (String prereq : entry.getValue()) {
                        if (!strongTopicNames.contains(prereq.toLowerCase())) {
                            prerequisiteSet.add(prereq + " (Prerequisite for " + gap.topic() + ")");
                        }
                    }
                }
            }
        }

        // 5. Prioritized Recommendations
        List<LearningGapReportDto.Recommendation> recommendations = new ArrayList<>();
        int rank = 1;

        // Priority A: Overdue Revisions
        for (LearningGapReportDto.RevisionNeedItem rev : revisionNeeds) {
            if (recommendations.size() < 6) {
                recommendations.add(new LearningGapReportDto.Recommendation(
                        rank++,
                        "Review Spaced Repetition: " + rev.topic(),
                        rev.topic(),
                        "SPACED_REVISION",
                        "Topic is due/overdue for review; spaced revision strengthens long-term memory retention.",
                        25,
                        "REVISE"
                ));
            }
        }

        // Priority B: Missing Core Role Skills
        for (String missing : missingSkills) {
            if (recommendations.size() < 6) {
                recommendations.add(new LearningGapReportDto.Recommendation(
                        rank++,
                        "Master Core Competency: " + missing,
                        missing,
                        "CAREER_READINESS",
                        "Essential required skill missing for " + targetRole + " benchmark.",
                        90,
                        "STUDY_TOPIC"
                ));
            }
        }

        // Priority C: Weak DSA or Profile areas
        for (LearningGapReportDto.GapItem gap : weakTopics) {
            if (recommendations.size() < 6 && !missingSkills.contains(gap.topic())) {
                recommendations.add(new LearningGapReportDto.Recommendation(
                        rank++,
                        "Strengthen Foundation: " + gap.topic(),
                        gap.topic(),
                        gap.category(),
                        gap.reason(),
                        60,
                        gap.category().contains("DSA") ? "PRACTICE_DSA" : "TAKE_QUIZ"
                ));
            }
        }

        // 6. Data Source Status Summary (Transparent reporting)
        List<LearningGapReportDto.DataSourceStatus> dataSources = List.of(
                new LearningGapReportDto.DataSourceStatus(
                        "Career Skills Inventory",
                        !userSkills.isEmpty(),
                        userSkills.size(),
                        userSkills.isEmpty() ? "No skills logged yet. Add skills in Career Hub to enrich gap analysis." : userSkills.size() + " verified/parsed technical skills analyzed."
                ),
                new LearningGapReportDto.DataSourceStatus(
                        "DSA & Coding Tracker",
                        !dsaProblems.isEmpty(),
                        dsaProblems.size(),
                        dsaProblems.isEmpty() ? "No DSA problems tracked yet. Log problem attempts in DSA Tracker to benchmark coding proficiency." : dsaProblems.size() + " DSA problems evaluated across topic categories."
                ),
                new LearningGapReportDto.DataSourceStatus(
                        "Learning Assessments",
                        !assessmentAttempts.isEmpty(),
                        assessmentAttempts.size(),
                        assessmentAttempts.isEmpty() ? "No quiz assessments completed yet. Take topic quizzes to assess concept mastery." : assessmentAttempts.size() + " assessment attempts evaluated."
                ),
                new LearningGapReportDto.DataSourceStatus(
                        "Spaced Revisions Engine",
                        !spacedRevisions.isEmpty() || !dsaRevisions.isEmpty(),
                        spacedRevisions.size() + dsaRevisions.size(),
                        (spacedRevisions.isEmpty() && dsaRevisions.isEmpty()) ? "No revision items scheduled." : (spacedRevisions.size() + dsaRevisions.size()) + " revision schedules active."
                ),
                new LearningGapReportDto.DataSourceStatus(
                        "Knowledge Hub Documents & Notes",
                        docCount > 0 || noteCount > 0,
                        (int) (docCount + noteCount),
                        (docCount == 0 && noteCount == 0) ? "No documents or notes in Knowledge Hub." : docCount + " documents and " + noteCount + " academic notes available."
                )
        );

        // Topic Mastery Map (Strictly grounded in user's skills, DSA solves, and assessment scores)
        Map<String, Integer> masteryMap = new HashMap<>();

        // 1. Core Programming & OOP
        int progScore = 0;
        if (strongTopicNames.contains("java") || strongTopicNames.contains("python") || strongTopicNames.contains("c++")) {
            progScore = 85;
        } else if (userSkills.stream().anyMatch(s -> s.getSkillName().toLowerCase().contains("java") || s.getSkillName().toLowerCase().contains("python"))) {
            progScore = 60;
        }
        masteryMap.put("Core Programming & OOP", progScore);

        // 2. Data Structures & Algorithms
        int dsaScore = 0;
        if (!dsaProblems.isEmpty()) {
            long solvedCount = dsaProblems.stream().filter(p -> p.getStatus() == DSAStatus.SOLVED).count();
            dsaScore = (int) Math.round(((double) solvedCount / dsaProblems.size()) * 100);
        }
        masteryMap.put("Data Structures & Algorithms", dsaScore);

        // 3. Databases & SQL
        int dbScore = 0;
        if (strongTopicNames.contains("sql") || strongTopicNames.contains("mysql") || strongTopicNames.contains("postgresql")) {
            dbScore = 85;
        } else if (userSkills.stream().anyMatch(s -> s.getSkillName().toLowerCase().contains("sql") || s.getSkillName().toLowerCase().contains("database"))) {
            dbScore = 60;
        }
        masteryMap.put("Databases & SQL", dbScore);

        // 4. System Design & Architecture
        int sysScore = 0;
        if (strongTopicNames.contains("system design") || strongTopicNames.contains("architecture")) {
            sysScore = 80;
        } else if (userSkills.stream().anyMatch(s -> s.getSkillName().toLowerCase().contains("system design"))) {
            sysScore = 55;
        }
        masteryMap.put("System Design & Architecture", sysScore);

        // 5. Cloud & DevOps
        int cloudScore = 0;
        if (strongTopicNames.contains("docker") || strongTopicNames.contains("aws") || strongTopicNames.contains("kubernetes")) {
            cloudScore = 80;
        } else if (userSkills.stream().anyMatch(s -> s.getSkillName().toLowerCase().contains("docker") || s.getSkillName().toLowerCase().contains("cloud"))) {
            cloudScore = 50;
        }
        masteryMap.put("Cloud & DevOps", cloudScore);

        int overallReadiness = (int) Math.round(roleReadiness.readinessPercentage());

        return new LearningGapReportDto.Response(
                targetRole,
                overallReadiness,
                strongTopics,
                weakTopics,
                missingSkills,
                revisionNeeds,
                new ArrayList<>(prerequisiteSet),
                recommendations,
                dataSources,
                masteryMap
        );
    }

    private String mapRoleNameToId(String targetRole) {
        if (targetRole == null) return "sde";
        String lower = targetRole.toLowerCase();
        if (lower.contains("sde") || lower.contains("software")) return "sde";
        if (lower.contains("java")) return "java-developer";
        if (lower.contains("full stack") || lower.contains("fullstack")) return "full-stack";
        if (lower.contains("front")) return "frontend-developer";
        if (lower.contains("back")) return "backend-engineer";
        if (lower.contains("ai") || lower.contains("machine learning") || lower.contains("ml")) return "ai-ml-engineer";
        if (lower.contains("devops") || lower.contains("cloud")) return "devops-engineer";
        if (lower.contains("data")) return "data-analyst";
        return "sde";
    }
}
