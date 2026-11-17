package com.aischolartwin.service;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeOrganizationService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeOrganizationService.class);

    private final KnowledgeOrganizationSuggestionRepository suggestionRepository;
    private final KnowledgeFolderRepository folderRepository;
    private final KnowledgeRuleService ruleService;
    private final DocumentRepository documentRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeFolderService folderService;
    private final KnowledgeTagRepository tagRepository;
    private final KnowledgeResourceTagRepository resourceTagRepository;
    private final ObjectMapper objectMapper;

    public KnowledgeOrganizationService(
            KnowledgeOrganizationSuggestionRepository suggestionRepository,
            KnowledgeFolderRepository folderRepository,
            KnowledgeRuleService ruleService,
            DocumentRepository documentRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            KnowledgeFolderService folderService,
            KnowledgeTagRepository tagRepository,
            KnowledgeResourceTagRepository resourceTagRepository,
            ObjectMapper objectMapper) {
        this.suggestionRepository = suggestionRepository;
        this.folderRepository = folderRepository;
        this.ruleService = ruleService;
        this.documentRepository = documentRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.folderService = folderService;
        this.tagRepository = tagRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<OrganizationSuggestionResponse> getPendingSuggestions(User user) {
        return suggestionRepository.findByUserAndStatusOrderByCreatedAtDesc(user, SuggestionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Evaluates rules and generates folder suggestions for a resource.
     * Can be invoked synchronously upon document upload or note creation.
     */
    @Transactional
    public KnowledgeOrganizationSuggestion generateSuggestion(User user, ResourceType type, Long resourceId,
                                                              String filename, String title, String content,
                                                              String fileExtension, List<String> extractedKeywords,
                                                              String subject) {
        // 1. Check user-defined rules first
        KnowledgeOrganizationRule matchedRule = ruleService.evaluateRules(user, type, filename, title, content, fileExtension);
        if (matchedRule != null) {
            KnowledgeFolder folder = matchedRule.getTargetFolder();
            String folderPath = folderService.computeFolderPath(folder);

            // If rule has autoMove enabled, move automatically without requiring confirmation
            if (matchedRule.isAutoMove()) {
                assignResourceToFolder(user, type, resourceId, folder.getId());
                log.info("Auto-moved resource {} (id: {}) to folder '{}' by rule '{}'",
                        type, resourceId, folder.getName(), matchedRule.getRuleName());
            }

            List<String> tags = new ArrayList<>();
            if (matchedRule.getSuggestedTags() != null && !matchedRule.getSuggestedTags().isBlank()) {
                for (String t : matchedRule.getSuggestedTags().split(",")) {
                    if (!t.trim().isEmpty()) tags.add(t.trim());
                }
            }

            KnowledgeOrganizationSuggestion suggestion = new KnowledgeOrganizationSuggestion(
                    user, type, resourceId,
                    folder.getId(), folderPath,
                    subject != null ? subject : "Academic",
                    null, "Academic Library",
                    serializeTags(tags),
                    0.95,
                    "Matched user-defined rule: " + matchedRule.getRuleName(),
                    false
            );
            if (matchedRule.isAutoMove()) {
                suggestion.setStatus(SuggestionStatus.ACCEPTED);
            }
            return suggestionRepository.save(suggestion);
        }

        // 2. Deterministic Academic Semantic Classifier
        List<KnowledgeFolder> userFolders = folderRepository.findByUserOrderByNameAsc(user);
        KnowledgeFolder bestFolder = null;
        double confidence = 0.5;
        String reasoning = "Deterministic academic keyword matching";

        String combinedText = ((filename != null ? filename : "") + " " +
                (title != null ? title : "") + " " +
                (content != null && content.length() > 500 ? content.substring(0, 500) : (content != null ? content : ""))).toLowerCase();

        // Check if any existing folder name matches words in filename or title
        for (KnowledgeFolder f : userFolders) {
            String folderNameLower = f.getName().toLowerCase();
            if (combinedText.contains(folderNameLower) && folderNameLower.length() > 2) {
                bestFolder = f;
                confidence = 0.85;
                reasoning = "Matched existing folder name: '" + f.getName() + "'";
                break;
            }
        }

        // Subject-based matching fallback if no direct folder name hit
        if (bestFolder == null && subject != null) {
            String cleanSubject = subject.toLowerCase();
            for (KnowledgeFolder f : userFolders) {
                if (cleanSubject.contains(f.getName().toLowerCase()) || f.getName().toLowerCase().contains("academic")) {
                    bestFolder = f;
                    confidence = 0.70;
                    reasoning = "Classified as subject '" + subject + "' matching folder '" + f.getName() + "'";
                    break;
                }
            }
        }

        List<String> tags = new ArrayList<>();
        if (extractedKeywords != null) {
            tags.addAll(extractedKeywords.stream().limit(5).toList());
        }

        KnowledgeOrganizationSuggestion suggestion = new KnowledgeOrganizationSuggestion(
                user, type, resourceId,
                bestFolder != null ? bestFolder.getId() : null,
                bestFolder != null ? folderService.computeFolderPath(bestFolder) : "Academic Library / Unassigned",
                subject != null ? subject : "General Academic",
                null,
                "Academic Library",
                serializeTags(tags),
                confidence,
                reasoning,
                false
        );

        return suggestionRepository.save(suggestion);
    }

    @Transactional
    public OrganizationSuggestionResponse acceptSuggestion(User user, Long id, SuggestionActionRequest request) {
        KnowledgeOrganizationSuggestion suggestion = findUserSuggestionOrThrow(user, id);

        Long targetFolderId = request != null && request.targetFolderId() != null
                ? request.targetFolderId()
                : suggestion.getSuggestedFolderId();

        if (targetFolderId != null) {
            assignResourceToFolder(user, suggestion.getResourceType(), suggestion.getResourceId(), targetFolderId);
        }

        // Apply tags if provided
        List<String> tagsToApply = request != null && request.tags() != null
                ? request.tags()
                : deserializeTags(suggestion.getSuggestedTagsJson());

        if (tagsToApply != null && !tagsToApply.isEmpty()) {
            for (String tagName : tagsToApply) {
                applyTagToResource(user, suggestion.getResourceType(), suggestion.getResourceId(), tagName);
            }
        }

        suggestion.setStatus(SuggestionStatus.ACCEPTED);
        suggestion = suggestionRepository.save(suggestion);
        log.info("Accepted suggestion id: {} for user {}", id, user.getEmail());
        return toResponse(suggestion);
    }

    @Transactional
    public OrganizationSuggestionResponse rejectSuggestion(User user, Long id) {
        KnowledgeOrganizationSuggestion suggestion = findUserSuggestionOrThrow(user, id);
        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestion = suggestionRepository.save(suggestion);
        return toResponse(suggestion);
    }

    @Transactional
    public OrganizationSuggestionResponse dismissSuggestion(User user, Long id) {
        KnowledgeOrganizationSuggestion suggestion = findUserSuggestionOrThrow(user, id);
        suggestion.setStatus(SuggestionStatus.DISMISSED);
        suggestion = suggestionRepository.save(suggestion);
        return toResponse(suggestion);
    }

    @Transactional
    public List<OrganizationSuggestionResponse> bulkOrganize(User user, BulkOrganizeRequest request) {
        if (request.suggestionIds() == null || request.suggestionIds().isEmpty()) {
            return List.of();
        }

        List<OrganizationSuggestionResponse> responses = new ArrayList<>();
        for (Long id : request.suggestionIds()) {
            try {
                if ("REJECT".equalsIgnoreCase(request.action())) {
                    responses.add(rejectSuggestion(user, id));
                } else if ("DISMISS".equalsIgnoreCase(request.action())) {
                    responses.add(dismissSuggestion(user, id));
                } else {
                    responses.add(acceptSuggestion(user, id, null));
                }
            } catch (Exception e) {
                log.warn("Error processing suggestion id {}: {}", id, e.getMessage());
            }
        }
        return responses;
    }

    private void assignResourceToFolder(User user, ResourceType type, Long resourceId, Long folderId) {
        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(resourceId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + resourceId + ")"));
            doc.setFolderId(folderId);
            documentRepository.save(doc);
        } else if (type == ResourceType.NOTE || type == ResourceType.LINK) {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(resourceId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + resourceId + ")"));
            item.setFolderId(folderId);
            knowledgeItemRepository.save(item);
        }
    }

    private void applyTagToResource(User user, ResourceType type, Long resourceId, String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) return;
        String clean = tagName.trim().toLowerCase();

        KnowledgeTag tag = tagRepository.findByUserAndNameIgnoreCase(user, clean)
                .orElseGet(() -> tagRepository.save(new KnowledgeTag(user, clean, "#178582")));

        if (resourceTagRepository.findByTagAndResourceTypeAndResourceId(tag, type, resourceId).isEmpty()) {
            resourceTagRepository.save(new KnowledgeResourceTag(tag, type, resourceId));
        }
    }

    private KnowledgeOrganizationSuggestion findUserSuggestionOrThrow(User user, Long id) {
        return suggestionRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found or access denied (id: " + id + ")"));
    }

    private String serializeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeTags(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private OrganizationSuggestionResponse toResponse(KnowledgeOrganizationSuggestion s) {
        String resTitle = "Resource #" + s.getResourceId();
        if (s.getResourceType() == ResourceType.DOCUMENT) {
            resTitle = documentRepository.findById(s.getResourceId())
                    .map(Document::getOriginalFilename).orElse(resTitle);
        } else {
            resTitle = knowledgeItemRepository.findById(s.getResourceId())
                    .map(KnowledgeItem::getTitle).orElse(resTitle);
        }

        String folderName = "Unassigned";
        if (s.getSuggestedFolderId() != null) {
            folderName = folderRepository.findById(s.getSuggestedFolderId())
                    .map(KnowledgeFolder::getName).orElse(folderName);
        }

        return new OrganizationSuggestionResponse(
                s.getId(),
                s.getResourceType(),
                s.getResourceId(),
                resTitle,
                s.getSuggestedFolderId(),
                folderName,
                s.getSuggestedFolderPath(),
                s.getSuggestedSubject(),
                s.getSuggestedSemester(),
                s.getSuggestedCategory(),
                deserializeTags(s.getSuggestedTagsJson()),
                s.getConfidenceScore(),
                s.getReasoning(),
                s.isAiGenerated(),
                s.getStatus(),
                s.getCreatedAt()
        );
    }
}

