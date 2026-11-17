package com.aischolartwin.service;

import com.aischolartwin.dto.KnowledgeItemRequest;
import com.aischolartwin.dto.KnowledgeItemResponse;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.exception.UserNotFoundException;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service managing Knowledge Items (Notes and Learning Links).
 * Enforces ownership boundary and strict validation for item types.
 */
@Service
public class KnowledgeItemService {

    private static final Pattern SAFE_URL_PATTERN = Pattern.compile(
        "^https?://[\\w\\.-]+(:\\d+)?(/.*)?$", Pattern.CASE_INSENSITIVE
    );

    private final KnowledgeItemRepository knowledgeItemRepository;
    private final UserRepository userRepository;
    private final KnowledgeOrganizationService organizationService;

    public KnowledgeItemService(
        KnowledgeItemRepository knowledgeItemRepository,
        UserRepository userRepository,
        KnowledgeOrganizationService organizationService
    ) {
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    public KnowledgeItemService(
        KnowledgeItemRepository knowledgeItemRepository,
        UserRepository userRepository
    ) {
        this(knowledgeItemRepository, userRepository, null);
    }

    private User getAuthenticatedUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
    }

    private void validateItemPayload(KnowledgeItemRequest request) {
        if (request.type() == KnowledgeItemType.NOTE) {
            if (request.content() == null || request.content().isBlank()) {
                throw new InvalidPayloadException("Note content cannot be empty.");
            }
        } else if (request.type() == KnowledgeItemType.LINK) {
            if (request.url() == null || request.url().isBlank()) {
                throw new InvalidPayloadException("URL cannot be empty for learning links.");
            }
            String trimmedUrl = request.url().trim();
            if (!trimmedUrl.toLowerCase().startsWith("http://") && !trimmedUrl.toLowerCase().startsWith("https://")) {
                throw new InvalidPayloadException("Invalid URL protocol. Only HTTP and HTTPS URLs are permitted.");
            }
            if (trimmedUrl.toLowerCase().startsWith("javascript:") || trimmedUrl.toLowerCase().startsWith("data:")) {
                throw new InvalidPayloadException("Dangerous URL schemes are strictly forbidden.");
            }
            try {
                URI uri = URI.create(trimmedUrl);
                if (uri.getHost() == null) {
                    throw new InvalidPayloadException("Invalid URL format: missing host.");
                }
            } catch (Exception ex) {
                throw new InvalidPayloadException("Malformed URL: " + ex.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<KnowledgeItemResponse> getItems(
        String userEmail,
        KnowledgeItemType type,
        String category,
        String search
    ) {
        User user = getAuthenticatedUser(userEmail);

        String cleanCategory = (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL"))
                ? category.trim()
                : null;
        String cleanSearch = (search != null && !search.isBlank())
                ? search.trim()
                : null;

        List<KnowledgeItem> items = knowledgeItemRepository.searchItems(user, type, cleanCategory, cleanSearch);
        return items.stream()
                .map(KnowledgeItemResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeItemResponse getItemById(Long id, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge item not found with ID: " + id));
        return KnowledgeItemResponse.fromEntity(item);
    }

    @Transactional
    public KnowledgeItemResponse createItem(String userEmail, KnowledgeItemRequest request) {
        User user = getAuthenticatedUser(userEmail);
        validateItemPayload(request);

        String cleanCategory = (request.category() != null && !request.category().isBlank())
                ? request.category().trim()
                : "General";

        KnowledgeItem item = new KnowledgeItem(
            user,
            request.type(),
            request.title().trim(),
            request.type() == KnowledgeItemType.NOTE ? request.content().trim() : null,
            request.type() == KnowledgeItemType.LINK ? request.url().trim() : null,
            request.description() != null ? request.description().trim() : null,
            cleanCategory
        );
        item.setFolderId(request.folderId());
        item.setConnectedDocumentId(request.connectedDocumentId());
        item.setConnectedPageNumber(request.connectedPageNumber());

        KnowledgeItem saved = knowledgeItemRepository.save(item);

        if (organizationService != null && saved.getFolderId() == null) {
            try {
                ResourceType rType = saved.getType() == KnowledgeItemType.LINK ? ResourceType.LINK : ResourceType.NOTE;
                organizationService.generateSuggestion(
                        user, rType, saved.getId(), null, saved.getTitle(),
                        saved.getContent() != null ? saved.getContent() : saved.getDescription(),
                        null, List.of(cleanCategory), cleanCategory
                );
            } catch (Exception ignored) {}
        }

        return KnowledgeItemResponse.fromEntity(saved);
    }

    @Transactional
    public KnowledgeItemResponse updateItem(Long id, String userEmail, KnowledgeItemRequest request) {
        User user = getAuthenticatedUser(userEmail);
        validateItemPayload(request);

        KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge item not found with ID: " + id));

        item.setType(request.type());
        item.setTitle(request.title().trim());
        item.setContent(request.type() == KnowledgeItemType.NOTE ? request.content().trim() : null);
        item.setUrl(request.type() == KnowledgeItemType.LINK ? request.url().trim() : null);
        item.setDescription(request.description() != null ? request.description().trim() : null);
        item.setCategory(request.category() != null && !request.category().isBlank() ? request.category().trim() : "General");
        if (request.folderId() != null) {
            item.setFolderId(request.folderId());
        }
        if (request.connectedDocumentId() != null) {
            item.setConnectedDocumentId(request.connectedDocumentId());
        }
        if (request.connectedPageNumber() != null) {
            item.setConnectedPageNumber(request.connectedPageNumber());
        }

        KnowledgeItem updated = knowledgeItemRepository.save(item);
        return KnowledgeItemResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteItem(Long id, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge item not found with ID: " + id));
        knowledgeItemRepository.delete(item);
    }
}

