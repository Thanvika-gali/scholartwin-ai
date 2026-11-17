package com.aischolartwin.service;

import com.aischolartwin.dto.OrganizationRuleRequest;
import com.aischolartwin.dto.OrganizationRuleResponse;
import com.aischolartwin.entity.KnowledgeFolder;
import com.aischolartwin.entity.KnowledgeOrganizationRule;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.KnowledgeOrganizationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KnowledgeRuleService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRuleService.class);

    private final KnowledgeOrganizationRuleRepository ruleRepository;
    private final KnowledgeFolderService folderService;

    public KnowledgeRuleService(KnowledgeOrganizationRuleRepository ruleRepository,
                                KnowledgeFolderService folderService) {
        this.ruleRepository = ruleRepository;
        this.folderService = folderService;
    }

    @Transactional(readOnly = true)
    public List<OrganizationRuleResponse> listRules(User user) {
        return ruleRepository.findByUserOrderByPriorityDescCreatedAtAsc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrganizationRuleResponse getRule(User user, Long id) {
        KnowledgeOrganizationRule rule = findUserRuleOrThrow(user, id);
        return toResponse(rule);
    }

    @Transactional
    public OrganizationRuleResponse createRule(User user, OrganizationRuleRequest request) {
        KnowledgeFolder targetFolder = folderService.findUserFolderOrThrow(user, request.targetFolderId());

        KnowledgeOrganizationRule rule = new KnowledgeOrganizationRule(
                user,
                request.ruleName(),
                request.fieldName(),
                request.operator(),
                request.conditionValue(),
                targetFolder,
                request.suggestedTags(),
                request.autoMove() != null ? request.autoMove() : false,
                request.priority() != null ? request.priority() : 0,
                request.isEnabled() != null ? request.isEnabled() : true
        );

        rule = ruleRepository.save(rule);
        log.info("Created organization rule '{}' (id: {}) for user {}", rule.getRuleName(), rule.getId(), user.getEmail());
        return toResponse(rule);
    }

    @Transactional
    public OrganizationRuleResponse updateRule(User user, Long id, OrganizationRuleRequest request) {
        KnowledgeOrganizationRule rule = findUserRuleOrThrow(user, id);

        if (request.ruleName() != null && !request.ruleName().isBlank()) {
            rule.setRuleName(request.ruleName());
        }
        if (request.fieldName() != null) {
            rule.setFieldName(request.fieldName());
        }
        if (request.operator() != null) {
            rule.setOperator(request.operator());
        }
        if (request.conditionValue() != null && !request.conditionValue().isBlank()) {
            rule.setConditionValue(request.conditionValue());
        }
        if (request.targetFolderId() != null) {
            KnowledgeFolder targetFolder = folderService.findUserFolderOrThrow(user, request.targetFolderId());
            rule.setTargetFolder(targetFolder);
        }
        if (request.suggestedTags() != null) {
            rule.setSuggestedTags(request.suggestedTags());
        }
        if (request.autoMove() != null) {
            rule.setAutoMove(request.autoMove());
        }
        if (request.priority() != null) {
            rule.setPriority(request.priority());
        }
        if (request.isEnabled() != null) {
            rule.setEnabled(request.isEnabled());
        }

        rule = ruleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void deleteRule(User user, Long id) {
        KnowledgeOrganizationRule rule = findUserRuleOrThrow(user, id);
        ruleRepository.delete(rule);
        log.info("Deleted rule id: {} for user {}", id, user.getEmail());
    }

    /**
     * Evaluates active rules against the resource metadata.
     * Returns the highest-priority matching rule, or null if no rule matched.
     */
    @Transactional(readOnly = true)
    public KnowledgeOrganizationRule evaluateRules(User user, ResourceType type, String filename, String title, String content, String fileExtension) {
        List<KnowledgeOrganizationRule> rules = ruleRepository.findByUserAndIsEnabledTrueOrderByPriorityDesc(user);

        for (KnowledgeOrganizationRule rule : rules) {
            String targetValue = null;
            switch (rule.getFieldName()) {
                case FILENAME -> targetValue = filename;
                case TITLE -> targetValue = title;
                case CONTENT -> targetValue = content;
                case FILE_TYPE -> targetValue = fileExtension;
            }

            if (targetValue != null && matchesCondition(targetValue, rule.getOperator().name(), rule.getConditionValue())) {
                return rule;
            }
        }
        return null;
    }

    private boolean matchesCondition(String text, String op, String conditionVal) {
        if (text == null || conditionVal == null) return false;
        String t = text.toLowerCase();
        String c = conditionVal.toLowerCase().trim();

        return switch (op.toUpperCase()) {
            case "CONTAINS" -> t.contains(c);
            case "EQUALS" -> t.equalsIgnoreCase(c);
            case "STARTS_WITH" -> t.startsWith(c);
            case "REGEX" -> {
                try {
                    yield Pattern.compile(conditionVal, Pattern.CASE_INSENSITIVE).matcher(text).find();
                } catch (Exception e) {
                    yield false;
                }
            }
            default -> false;
        };
    }

    public KnowledgeOrganizationRule findUserRuleOrThrow(User user, Long id) {
        return ruleRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found or access denied (id: " + id + ")"));
    }

    private OrganizationRuleResponse toResponse(KnowledgeOrganizationRule r) {
        return new OrganizationRuleResponse(
                r.getId(),
                r.getRuleName(),
                r.getFieldName(),
                r.getOperator(),
                r.getConditionValue(),
                r.getTargetFolder() != null ? r.getTargetFolder().getId() : null,
                r.getTargetFolder() != null ? r.getTargetFolder().getName() : "Unassigned",
                r.getSuggestedTags(),
                r.isAutoMove(),
                r.getPriority(),
                r.isEnabled(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}

