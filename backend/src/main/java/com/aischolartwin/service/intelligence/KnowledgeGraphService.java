package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.KnowledgeGraphDtos.*;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.entity.learning.LearningTask;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.intelligence.KnowledgeNodeRepository;
import com.aischolartwin.repository.intelligence.KnowledgeRelationshipRepository;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KnowledgeGraphService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeGraphService.class);

    private final KnowledgeNodeRepository nodeRepository;
    private final KnowledgeRelationshipRepository relationshipRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final LearningProfileRepository profileRepository;
    private final LearningTaskRepository taskRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final DSAProblemRepository dsaProblemRepository;

    public KnowledgeGraphService(KnowledgeNodeRepository nodeRepository,
                                 KnowledgeRelationshipRepository relationshipRepository,
                                 CareerSkillRepository careerSkillRepository,
                                 LearningProfileRepository profileRepository,
                                 LearningTaskRepository taskRepository,
                                 KnowledgeItemRepository knowledgeItemRepository,
                                 DSAProblemRepository dsaProblemRepository) {
        this.nodeRepository = nodeRepository;
        this.relationshipRepository = relationshipRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.dsaProblemRepository = dsaProblemRepository;
    }

    @Transactional
    public KnowledgeGraphResponse getGraph(User user, String search, String category) {
        List<KnowledgeNode> nodes = (search != null && !search.isBlank())
                ? nodeRepository.searchNodes(user, search.trim())
                : nodeRepository.findByUserOrderByCategoryAscNameAsc(user);

        // If nodes empty, auto sync graph from actual database entities
        if (nodes.isEmpty()) {
            syncGraph(user);
            nodes = nodeRepository.findByUserOrderByCategoryAscNameAsc(user);
        }

        if (category != null && !category.isBlank()) {
            nodes = nodes.stream()
                    .filter(n -> category.equalsIgnoreCase(n.getCategory()) || category.equalsIgnoreCase(n.getNodeType().name()))
                    .toList();
        }

        Set<Long> nodeIds = new HashSet<>();
        for (KnowledgeNode n : nodes) {
            nodeIds.add(n.getId());
        }

        List<KnowledgeRelationship> allRels = relationshipRepository.findByUser(user);
        List<KnowledgeRelationship> filteredRels = allRels.stream()
                .filter(r -> nodeIds.contains(r.getSourceNode().getId()) && nodeIds.contains(r.getTargetNode().getId()))
                .toList();

        List<GraphNodeDto> nodeDtos = nodes.stream().map(GraphNodeDto::fromEntity).toList();
        List<GraphEdgeDto> edgeDtos = filteredRels.stream().map(GraphEdgeDto::fromEntity).toList();

        return new KnowledgeGraphResponse(nodeDtos.size(), edgeDtos.size(), nodeDtos, edgeDtos);
    }

    @Transactional(readOnly = true)
    public GraphNodeDto getNodeDetail(User user, Long id) {
        KnowledgeNode node = nodeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge node not found with ID: " + id));
        return GraphNodeDto.fromEntity(node);
    }

    @Transactional
    public KnowledgeGraphResponse syncGraph(User user) {
        Map<String, KnowledgeNode> nodeMap = new HashMap<>();

        // Load existing nodes
        for (KnowledgeNode n : nodeRepository.findByUserOrderByCategoryAscNameAsc(user)) {
            nodeMap.put(n.getName().toLowerCase(), n);
        }

        // 1. Target Role Node
        String targetRole = profileRepository.findByUser(user)
                .map(LearningProfile::getTargetRole)
                .orElse("Software Development Engineer");

        KnowledgeNode roleNode = getOrCreateNode(user, nodeMap, targetRole, NodeType.CAREER_GOAL,
                "Target professional role and industry benchmark", "Career", "ADVANCED", null, "CAREER_GOAL");

        // 2. Career Skills Nodes & Edge to Target Role
        try {
            List<CareerSkill> skills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
            for (CareerSkill skill : skills) {
                if (skill.getSkillName() != null && !skill.getSkillName().isBlank()) {
                    String name = skill.getSkillName().trim();
                    String level = skill.getProficiency() != null ? skill.getProficiency().name() : "INTERMEDIATE";
                    KnowledgeNode skillNode = getOrCreateNode(user, nodeMap, name, NodeType.SKILL,
                            "Technical proficiency in " + name, "Technology", level, skill.getId(), "CAREER_SKILL");

                    createRelationshipIfMissing(user, skillNode, roleNode, RelationType.REQUIRES,
                            roleNode.getName() + " requires mastery of " + name, 0.9, "Career Skill Match");
                }
            }
        } catch (Exception e) {
            log.debug("Skills sync skipped: {}", e.getMessage());
        }

        // 3. Dynamic Algorithmic Topics from User's DSA Tracker
        try {
            var userProblems = dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user);
            Set<String> dsaTopics = new LinkedHashSet<>();
            for (var p : userProblems) {
                if (p.getPrimaryTopic() != null && !p.getPrimaryTopic().isBlank()) {
                    dsaTopics.add(p.getPrimaryTopic().trim());
                }
            }

            for (String pattern : dsaTopics) {
                KnowledgeNode patternNode = getOrCreateNode(user, nodeMap, pattern, NodeType.DSA_TOPIC,
                        "Dynamic algorithmic topic tracked in user's DSA problem catalog", "Algorithms", "INTERMEDIATE", null, "DSA_TOPIC");

                KnowledgeNode javaNode = nodeMap.get("java");
                if (javaNode != null) {
                    createRelationshipIfMissing(user, patternNode, javaNode, RelationType.IMPLEMENTS,
                            pattern + " implemented in " + javaNode.getName(), 0.85, "Code Architecture");
                }
                createRelationshipIfMissing(user, patternNode, roleNode, RelationType.APPLIES_TO,
                        "Evaluated during technical coding interviews for " + roleNode.getName(), 0.88, "Interview Requirement");
            }
        } catch (Exception e) {
            log.debug("DSA topics sync skipped: {}", e.getMessage());
        }

        // 4. Learning Tasks Nodes
        try {
            List<LearningTask> tasks = taskRepository.findByUser(user);
            for (LearningTask task : tasks.stream().limit(6).toList()) {
                KnowledgeNode taskNode = getOrCreateNode(user, nodeMap, task.getTitle(), NodeType.LEARNING_TASK,
                        task.getLearningObjectives() != null ? task.getLearningObjectives() : "Structured roadmap task",
                        "Learning", task.getStatus().name(), task.getId(), "LEARNING_TASK");

                createRelationshipIfMissing(user, taskNode, roleNode, RelationType.APPLIES_TO,
                        "Milestone progress for " + roleNode.getName(), 0.8, "Roadmap Curriculum");

                if (task.getTopic() != null) {
                    KnowledgeNode topicNode = nodeMap.get(task.getTopic().toLowerCase());
                    if (topicNode != null) {
                        createRelationshipIfMissing(user, taskNode, topicNode, RelationType.REINFORCES,
                                "Hands-on exercises reinforce " + topicNode.getName(), 0.9, "Practical Application");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Tasks sync skipped: {}", e.getMessage());
        }

        // 5. Knowledge Vault Items Nodes
        try {
            List<KnowledgeItem> notes = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user);
            for (KnowledgeItem note : notes.stream().limit(5).toList()) {
                KnowledgeNode noteNode = getOrCreateNode(user, nodeMap, note.getTitle(), NodeType.DOCUMENT,
                        "Verified study note preserved in Knowledge Vault", "Knowledge", "STUDIED", note.getId(), "KNOWLEDGE_ITEM");

                createRelationshipIfMissing(user, noteNode, roleNode, RelationType.REFERENCES,
                        "Study reference for career and academic milestones", 0.75, "Knowledge Vault");
            }
        } catch (Exception e) {
            log.debug("Knowledge notes sync skipped: {}", e.getMessage());
        }

        return getGraph(user, null, null);
    }

    private KnowledgeNode getOrCreateNode(User user, Map<String, KnowledgeNode> map, String name, NodeType type,
                                          String description, String category, String mastery, Long refId, String refType) {
        String key = name.toLowerCase();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        var opt = nodeRepository.findByNameIgnoreCaseAndUser(name, user);
        if (opt.isPresent()) {
            map.put(key, opt.get());
            return opt.get();
        }

        KnowledgeNode created = new KnowledgeNode(user, name, type, description, category, mastery, refId, refType);
        KnowledgeNode saved = nodeRepository.save(created);
        map.put(key, saved);
        return saved;
    }

    private void createRelationshipIfMissing(User user, KnowledgeNode source, KnowledgeNode target,
                                             RelationType type, String description, Double strength, String sourceName) {
        if (source == null || target == null || source.getId().equals(target.getId())) {
            return;
        }
        boolean exists = relationshipRepository.existsByUserAndSourceNodeAndTargetNodeAndRelationType(user, source, target, type);
        if (!exists) {
            KnowledgeRelationship rel = new KnowledgeRelationship(user, source, target, type, description, strength, sourceName);
            relationshipRepository.save(rel);
        }
    }
}
