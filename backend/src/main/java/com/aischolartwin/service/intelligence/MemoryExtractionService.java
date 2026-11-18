package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.*;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import com.aischolartwin.repository.intelligence.UserMemoryRepository;
import com.aischolartwin.repository.learning.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MemoryExtractionService {

    private static final Logger log = LoggerFactory.getLogger(MemoryExtractionService.class);

    private final UserMemoryRepository memoryRepository;
    private final MemoryPreferenceRepository preferenceRepository;
    private final LearningProfileRepository profileRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final LearningTaskRepository taskRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final DSAProblemRepository dsaProblemRepository;

    public MemoryExtractionService(UserMemoryRepository memoryRepository,
                                  MemoryPreferenceRepository preferenceRepository,
                                  LearningProfileRepository profileRepository,
                                  LearningRoadmapRepository roadmapRepository,
                                  LearningTaskRepository taskRepository,
                                  CareerSkillRepository careerSkillRepository,
                                  KnowledgeItemRepository knowledgeItemRepository,
                                  DSAProblemRepository dsaProblemRepository) {
        this.memoryRepository = memoryRepository;
        this.preferenceRepository = preferenceRepository;
        this.profileRepository = profileRepository;
        this.roadmapRepository = roadmapRepository;
        this.taskRepository = taskRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.dsaProblemRepository = dsaProblemRepository;
    }

    @Transactional
    public MemoryDtos.MemoryExtractionResult extractMemoriesFromActivity(User user) {
        MemoryPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> preferenceRepository.save(new MemoryPreference(user)));

        if (!Boolean.TRUE.equals(pref.getMemoryEnabled()) || !Boolean.TRUE.equals(pref.getAutoExtractFromActivity())) {
            log.info("Memory auto-extraction disabled for user: {}", user.getEmail());
            return new MemoryDtos.MemoryExtractionResult(0, 0, List.of("Auto-extraction disabled in user preferences."));
        }

        List<UserMemory> candidates = new ArrayList<>();

        // 1. Learning Profile Extraction (Career & Learning Memory)
        if (Boolean.TRUE.equals(pref.getCareerMemoryEnabled())) {
            profileRepository.findByUser(user).ifPresent(p -> {
                if (p.getTargetRole() != null && !p.getTargetRole().isBlank()) {
                    String roleContent = "User is preparing for the target role: " + p.getTargetRole().trim() + ".";
                    addCandidateIfUnique(user, candidates, MemoryCategory.CAREER, "Target Role: " + p.getTargetRole().trim(),
                            roleContent, MemoryType.GOAL, MemorySourceType.PROFILE, "Learning Profile", 0.95, MemoryImportance.HIGH);
                }
                if (p.getLearningGoals() != null && !p.getLearningGoals().isBlank()) {
                    String goalContent = "Current primary academic/career goal: " + p.getLearningGoals().trim() + ".";
                    addCandidateIfUnique(user, candidates, MemoryCategory.CAREER, "Primary Goal",
                            goalContent, MemoryType.GOAL, MemorySourceType.PROFILE, "Learning Profile", 0.95, MemoryImportance.HIGH);
                }
            });
        }

        // 2. Learning Profile Weak Areas (Learning Memory)
        if (Boolean.TRUE.equals(pref.getLearningMemoryEnabled())) {
            profileRepository.findByUser(user).ifPresent(p -> {
                if (p.getWeakAreas() != null && !p.getWeakAreas().isBlank()) {
                    String raw = p.getWeakAreas().replace("[", "").replace("]", "").replace("\"", "");
                    String[] areas = raw.split(",");
                    for (String area : areas) {
                        String trimmed = area.trim();
                        if (!trimmed.isBlank()) {
                            String content = "Diagnosed weak area requiring active revision: " + trimmed + ".";
                            addCandidateIfUnique(user, candidates, MemoryCategory.LEARNING, "Weak Concept: " + trimmed,
                                    content, MemoryType.WEAKNESS, MemorySourceType.PROFILE, "Profile Diagnostics", 0.85, MemoryImportance.HIGH);
                        }
                    }
                }
            });
        }

        // 3. Technical Skills (Technical Memory)
        if (Boolean.TRUE.equals(pref.getTechnicalMemoryEnabled())) {
            try {
                List<CareerSkill> skills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
                for (CareerSkill skill : skills) {
                    if (skill.getSkillName() != null && !skill.getSkillName().isBlank()) {
                        String level = skill.getProficiency() != null ? skill.getProficiency().name() : "INTERMEDIATE";
                        String content = "Possesses technical skill in " + skill.getSkillName().trim() + " with " + level + " proficiency.";
                        addCandidateIfUnique(user, candidates, MemoryCategory.TECHNICAL, "Skill: " + skill.getSkillName().trim(),
                                content, MemoryType.STRENGTH, MemorySourceType.RESUME, "Career Skills", 0.90, MemoryImportance.MEDIUM);
                    }
                }
            } catch (Exception e) {
                log.debug("Career skills scan skipped: {}", e.getMessage());
            }
        }

        // 4. Learning Roadmaps & Milestones (Learning Memory)
        if (Boolean.TRUE.equals(pref.getLearningMemoryEnabled())) {
            try {
                List<LearningRoadmap> roadmaps = roadmapRepository.findByUserOrderByCreatedAtDesc(user);
                for (LearningRoadmap rm : roadmaps) {
                    if (rm.getStatus() == RoadmapStatus.IN_PROGRESS || rm.getStatus() == RoadmapStatus.COMPLETED) {
                        String content = "Engaged in learning roadmap: " + rm.getTitle() + " (" + rm.getCompletionPercentage() + "% complete).";
                        addCandidateIfUnique(user, candidates, MemoryCategory.LEARNING, "Roadmap: " + rm.getTitle(),
                                content, MemoryType.PROJECT_CONTEXT, MemorySourceType.LEARNING_TASK, "Roadmap Engine", 0.85, MemoryImportance.MEDIUM);
                    }
                }
            } catch (Exception e) {
                log.debug("Roadmaps scan skipped: {}", e.getMessage());
            }
        }

        // 5. Knowledge Hub Important Notes (Knowledge Memory)
        if (Boolean.TRUE.equals(pref.getKnowledgeMemoryEnabled())) {
            try {
                List<KnowledgeItem> items = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user);
                for (KnowledgeItem item : items.stream().limit(5).toList()) {
                    if (item.getTitle() != null && !item.getTitle().isBlank()) {
                        String content = "Key knowledge note recorded in vault: '" + item.getTitle().trim() + "' in category: " +
                                (item.getCategory() != null ? item.getCategory() : "General") + ".";
                        addCandidateIfUnique(user, candidates, MemoryCategory.KNOWLEDGE, "Note: " + item.getTitle().trim(),
                                content, MemoryType.FACT, MemorySourceType.KNOWLEDGE_ITEM, "Knowledge Vault", 0.80, MemoryImportance.LOW);
                    }
                }
            } catch (Exception e) {
                log.debug("Knowledge items scan skipped: {}", e.getMessage());
            }
        }

        int extracted = candidates.size();
        List<String> samples = new ArrayList<>();
        if (!candidates.isEmpty()) {
            List<UserMemory> saved = memoryRepository.saveAll(candidates);
            for (UserMemory m : saved) {
                if (samples.size() < 4) {
                    samples.add(m.getSummary() != null ? m.getSummary() : m.getContent());
                }
            }
            log.info("Extracted and saved {} candidate memories for user: {}", saved.size(), user.getEmail());
        }

        return new MemoryDtos.MemoryExtractionResult(extracted, 0, samples);
    }

    @Transactional
    public MemoryDtos.MemoryExtractionResult consolidateMemories(User user) {
        List<UserMemory> active = memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(user, MemoryStatus.ACTIVE);
        Set<String> seenContents = new HashSet<>();
        List<UserMemory> toRemove = new ArrayList<>();

        for (UserMemory m : active) {
            String norm = m.getContent().trim().toLowerCase();
            if (seenContents.contains(norm)) {
                toRemove.add(m);
            } else {
                seenContents.add(norm);
            }
        }

        if (!toRemove.isEmpty()) {
            memoryRepository.deleteAll(toRemove);
            log.info("Consolidated and removed {} duplicate memories for user: {}", toRemove.size(), user.getEmail());
        }

        return new MemoryDtos.MemoryExtractionResult(0, toRemove.size(),
                List.of("Consolidation complete: " + toRemove.size() + " redundant memories pruned."));
    }

    private void addCandidateIfUnique(User user, List<UserMemory> targetList,
                                      MemoryCategory category, String summary, String content,
                                      MemoryType type, MemorySourceType sourceType, String sourceRef,
                                      Double confidence, MemoryImportance importance) {
        String safeContent = MemoryService.redactSensitiveData(content.trim());
        String safeSummary = MemoryService.redactSensitiveData(summary != null ? summary.trim() : null);

        boolean exists = memoryRepository.existsByUserAndContentIgnoreCaseAndStatusNot(user, safeContent, MemoryStatus.DELETED);
        if (exists) {
            return;
        }
        for (UserMemory m : targetList) {
            if (m.getContent().equalsIgnoreCase(safeContent)) {
                return;
            }
        }

        UserMemory mem = new UserMemory(
                user,
                category,
                safeSummary,
                safeContent,
                type,
                MemoryProvenance.INFERRED_FROM_ACTIVITY,
                sourceType,
                sourceRef,
                confidence,
                importance
        );
        targetList.add(mem);
    }
}
