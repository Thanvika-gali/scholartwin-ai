package com.aischolartwin.service;

import com.aischolartwin.dto.CollectionItemRequest;
import com.aischolartwin.dto.ResourceBulkActionRequest;
import com.aischolartwin.dto.ResourceItemResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeResourceService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeResourceService.class);

    private final DocumentRepository documentRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeFolderRepository folderRepository;
    private final KnowledgeFolderService folderService;
    private final KnowledgeTagRepository tagRepository;
    private final KnowledgeResourceTagRepository resourceTagRepository;
    private final KnowledgeViewHistoryRepository viewHistoryRepository;

    public KnowledgeResourceService(
            DocumentRepository documentRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            KnowledgeFolderRepository folderRepository,
            KnowledgeFolderService folderService,
            KnowledgeTagRepository tagRepository,
            KnowledgeResourceTagRepository resourceTagRepository,
            KnowledgeViewHistoryRepository viewHistoryRepository) {
        this.documentRepository = documentRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.folderRepository = folderRepository;
        this.folderService = folderService;
        this.tagRepository = tagRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.viewHistoryRepository = viewHistoryRepository;
    }

    @Transactional(readOnly = true)
    public ResourceItemResponse getResourceItem(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
            return mapDocumentToResource(user, doc);
        } else {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
            return mapItemToResource(user, item);
        }
    }

    @Transactional(readOnly = true)
    public void verifyResourceOwnership(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
        } else {
            knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
        }
    }

    @Transactional(readOnly = true)
    public List<ResourceItemResponse> listResources(
            User user,
            ResourceType type,
            Long folderId,
            Boolean inboxOnly,
            Boolean favoritesOnly,
            Boolean pinnedOnly,
            Boolean archivedOnly,
            String search,
            String sortBy
    ) {
        List<ResourceItemResponse> results = new ArrayList<>();
        boolean isArchivedTarget = Boolean.TRUE.equals(archivedOnly);

        // Fetch Documents
        if (type == null || type == ResourceType.DOCUMENT) {
            List<Document> docs = documentRepository.findByUserOrderByUpdatedAtDesc(user);
            for (Document d : docs) {
                if (isArchivedTarget != d.isArchived()) continue;
                if (Boolean.TRUE.equals(inboxOnly) && d.getFolderId() != null) continue;
                if (!Boolean.TRUE.equals(inboxOnly) && folderId != null && !Objects.equals(d.getFolderId(), folderId)) continue;
                if (Boolean.TRUE.equals(favoritesOnly) && !d.isFavorite()) continue;
                if (Boolean.TRUE.equals(pinnedOnly) && !d.isPinned()) continue;
                if (search != null && !search.isBlank()) {
                    String q = search.toLowerCase();
                    boolean match = d.getOriginalFilename().toLowerCase().contains(q) ||
                            (d.getDocumentSummary() != null && d.getDocumentSummary().toLowerCase().contains(q));
                    if (!match) continue;
                }
                results.add(mapDocumentToResource(user, d));
            }
        }

        // Fetch KnowledgeItems (Notes and Links)
        if (type == null || type == ResourceType.NOTE || type == ResourceType.LINK) {
            List<KnowledgeItem> items = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user);
            for (KnowledgeItem i : items) {
                if (type != null && i.getType() != KnowledgeItemType.valueOf(type.name())) continue;
                if (isArchivedTarget != i.isArchived()) continue;
                if (Boolean.TRUE.equals(inboxOnly) && i.getFolderId() != null) continue;
                if (!Boolean.TRUE.equals(inboxOnly) && folderId != null && !Objects.equals(i.getFolderId(), folderId)) continue;
                if (Boolean.TRUE.equals(favoritesOnly) && !i.isFavorite()) continue;
                if (Boolean.TRUE.equals(pinnedOnly) && !i.isPinned()) continue;
                if (search != null && !search.isBlank()) {
                    String q = search.toLowerCase();
                    boolean match = (i.getTitle() != null && i.getTitle().toLowerCase().contains(q)) ||
                            (i.getContent() != null && i.getContent().toLowerCase().contains(q)) ||
                            (i.getDescription() != null && i.getDescription().toLowerCase().contains(q)) ||
                            (i.getUrl() != null && i.getUrl().toLowerCase().contains(q));
                    if (!match) continue;
                }
                results.add(mapItemToResource(user, i));
            }
        }

        // Sorting
        Comparator<ResourceItemResponse> comparator = Comparator.comparing(ResourceItemResponse::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        if ("NAME".equalsIgnoreCase(sortBy) || "ALPHABETICAL".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(r -> r.title() != null ? r.title().toLowerCase() : "");
        } else if ("RECENT_ADDED".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(ResourceItemResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("RECENT_VIEWED".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(ResourceItemResponse::lastViewedAt, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("SIZE".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(ResourceItemResponse::fileSizeBytes, Comparator.reverseOrder());
        }

        results.sort(comparator);
        return results;
    }

    @Transactional
    public ResourceItemResponse toggleFavorite(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
            doc.setFavorite(!doc.isFavorite());
            doc = documentRepository.save(doc);
            return mapDocumentToResource(user, doc);
        } else {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
            item.setFavorite(!item.isFavorite());
            item = knowledgeItemRepository.save(item);
            return mapItemToResource(user, item);
        }
    }

    @Transactional
    public ResourceItemResponse togglePin(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
            doc.setPinned(!doc.isPinned());
            doc = documentRepository.save(doc);
            return mapDocumentToResource(user, doc);
        } else {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
            item.setPinned(!item.isPinned());
            item = knowledgeItemRepository.save(item);
            return mapItemToResource(user, item);
        }
    }

    @Transactional
    public ResourceItemResponse toggleArchive(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
            doc.setArchived(!doc.isArchived());
            doc = documentRepository.save(doc);
            return mapDocumentToResource(user, doc);
        } else {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
            item.setArchived(!item.isArchived());
            item = knowledgeItemRepository.save(item);
            return mapItemToResource(user, item);
        }
    }

    @Transactional
    public ResourceItemResponse moveResource(User user, ResourceType type, Long id, Long targetFolderId) {
        if (targetFolderId != null) {
            folderService.findUserFolderOrThrow(user, targetFolderId);
        }

        if (type == ResourceType.DOCUMENT) {
            Document doc = documentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + id + ")"));
            doc.setFolderId(targetFolderId);
            doc = documentRepository.save(doc);
            return mapDocumentToResource(user, doc);
        } else {
            KnowledgeItem item = knowledgeItemRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found (id: " + id + ")"));
            item.setFolderId(targetFolderId);
            item = knowledgeItemRepository.save(item);
            return mapItemToResource(user, item);
        }
    }

    @Transactional
    public void recordView(User user, ResourceType type, Long id) {
        verifyResourceOwnership(user, type, id);
        Instant now = Instant.now();

        if (type == ResourceType.DOCUMENT) {
            documentRepository.findByIdAndUser(id, user).ifPresent(d -> {
                d.setLastViewedAt(now);
                documentRepository.save(d);
            });
        } else {
            knowledgeItemRepository.findByIdAndUser(id, user).ifPresent(i -> {
                i.setLastViewedAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
                knowledgeItemRepository.save(i);
            });
        }
        viewHistoryRepository.save(new KnowledgeViewHistory(user, type, id));
    }

    @Transactional(readOnly = true)
    public List<ResourceItemResponse> getRecentlyViewed(User user) {
        List<KnowledgeViewHistory> history = viewHistoryRepository.findTop30ByUserOrderByViewedAtDesc(user);
        Set<String> seen = new HashSet<>();
        List<ResourceItemResponse> results = new ArrayList<>();

        for (KnowledgeViewHistory h : history) {
            String key = h.getResourceType().name() + ":" + h.getResourceId();
            if (seen.add(key)) {
                try {
                    ResourceItemResponse item = getResourceItem(user, h.getResourceType(), h.getResourceId());
                    if (item != null) results.add(item);
                } catch (Exception ignored) {}
            }
        }
        return results;
    }

    @Transactional
    public void executeBulkAction(User user, ResourceBulkActionRequest request) {
        if (request.items() == null || request.items().isEmpty()) return;
        String action = request.action() != null ? request.action().toUpperCase() : "";

        for (CollectionItemRequest item : request.items()) {
            try {
                switch (action) {
                    case "MOVE" -> moveResource(user, item.resourceType(), item.resourceId(), request.targetFolderId());
                    case "FAVORITE" -> setFavoriteState(user, item.resourceType(), item.resourceId(), true);
                    case "UNFAVORITE" -> setFavoriteState(user, item.resourceType(), item.resourceId(), false);
                    case "ARCHIVE" -> setArchiveState(user, item.resourceType(), item.resourceId(), true);
                    case "UNARCHIVE" -> setArchiveState(user, item.resourceType(), item.resourceId(), false);
                    case "TAG" -> applyTags(user, item.resourceType(), item.resourceId(), request.tags());
                    case "DELETE" -> deleteResourceSafely(user, item.resourceType(), item.resourceId());
                }
            } catch (Exception e) {
                log.warn("Bulk action {} failed on {} #{}: {}", action, item.resourceType(), item.resourceId(), e.getMessage());
            }
        }
    }

    private void setFavoriteState(User user, ResourceType type, Long id, boolean state) {
        if (type == ResourceType.DOCUMENT) {
            documentRepository.findByIdAndUser(id, user).ifPresent(d -> { d.setFavorite(state); documentRepository.save(d); });
        } else {
            knowledgeItemRepository.findByIdAndUser(id, user).ifPresent(i -> { i.setFavorite(state); knowledgeItemRepository.save(i); });
        }
    }

    private void setArchiveState(User user, ResourceType type, Long id, boolean state) {
        if (type == ResourceType.DOCUMENT) {
            documentRepository.findByIdAndUser(id, user).ifPresent(d -> { d.setArchived(state); documentRepository.save(d); });
        } else {
            knowledgeItemRepository.findByIdAndUser(id, user).ifPresent(i -> { i.setArchived(state); knowledgeItemRepository.save(i); });
        }
    }

    private void applyTags(User user, ResourceType type, Long id, List<String> tags) {
        if (tags == null) return;
        for (String tagName : tags) {
            if (tagName.trim().isEmpty()) continue;
            String clean = tagName.trim().toLowerCase();
            KnowledgeTag tag = tagRepository.findByUserAndNameIgnoreCase(user, clean)
                    .orElseGet(() -> tagRepository.save(new KnowledgeTag(user, clean, "#178582")));

            if (resourceTagRepository.findByTagAndResourceTypeAndResourceId(tag, type, id).isEmpty()) {
                resourceTagRepository.save(new KnowledgeResourceTag(tag, type, id));
            }
        }
    }

    private void deleteResourceSafely(User user, ResourceType type, Long id) {
        if (type == ResourceType.DOCUMENT) {
            documentRepository.findByIdAndUser(id, user).ifPresent(documentRepository::delete);
        } else {
            knowledgeItemRepository.findByIdAndUser(id, user).ifPresent(knowledgeItemRepository::delete);
        }
    }

    public ResourceItemResponse mapDocumentToResource(User user, Document d) {
        String folderName = "Smart Inbox";
        String folderPath = "Smart Inbox";
        if (d.getFolderId() != null) {
            Optional<KnowledgeFolder> fOpt = folderRepository.findByUserAndId(user, d.getFolderId());
            if (fOpt.isPresent()) {
                folderName = fOpt.get().getName();
                folderPath = folderService.computeFolderPath(fOpt.get());
            }
        }

        List<String> tags = resourceTagRepository.findTagsByResource(ResourceType.DOCUMENT, d.getId())
                .stream().map(KnowledgeTag::getName).collect(Collectors.toList());

        return new ResourceItemResponse(
                ResourceType.DOCUMENT,
                d.getId(),
                d.getOriginalFilename(),
                d.getDocumentSummary() != null ? d.getDocumentSummary() : ("Academic " + d.getFileExtension().toUpperCase() + " document"),
                null,
                null,
                "Academic Document",
                d.getFolderId(),
                folderName,
                folderPath,
                d.getFileExtension(),
                formatBytes(d.getFileSize()),
                d.getFileSize(),
                d.getPageCount(),
                d.getStatus().name(),
                d.isFavorite(),
                d.isPinned(),
                d.isArchived(),
                tags,
                d.getDocumentSummary(),
                d.getVersionNumber(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                d.getLastViewedAt()
        );
    }

    public ResourceItemResponse mapItemToResource(User user, KnowledgeItem i) {
        String folderName = "Smart Inbox";
        String folderPath = "Smart Inbox";
        if (i.getFolderId() != null) {
            Optional<KnowledgeFolder> fOpt = folderRepository.findByUserAndId(user, i.getFolderId());
            if (fOpt.isPresent()) {
                folderName = fOpt.get().getName();
                folderPath = folderService.computeFolderPath(fOpt.get());
            }
        }

        ResourceType resType = i.getType() == KnowledgeItemType.LINK ? ResourceType.LINK : ResourceType.NOTE;
        List<String> tags = resourceTagRepository.findTagsByResource(resType, i.getId())
                .stream().map(KnowledgeTag::getName).collect(Collectors.toList());

        Instant createdAt = i.getCreatedAt() != null ? i.getCreatedAt().toInstant(ZoneOffset.UTC) : Instant.now();
        Instant updatedAt = i.getUpdatedAt() != null ? i.getUpdatedAt().toInstant(ZoneOffset.UTC) : Instant.now();
        Instant lastViewed = i.getLastViewedAt() != null ? i.getLastViewedAt().toInstant(ZoneOffset.UTC) : null;

        return new ResourceItemResponse(
                resType,
                i.getId(),
                i.getTitle(),
                i.getDescription() != null ? i.getDescription() : (i.getContent() != null && i.getContent().length() > 140 ? i.getContent().substring(0, 140) + "..." : i.getContent()),
                i.getContent(),
                i.getUrl(),
                i.getCategory(),
                i.getFolderId(),
                folderName,
                folderPath,
                null,
                null,
                0,
                null,
                "PARSED",
                i.isFavorite(),
                i.isPinned(),
                i.isArchived(),
                tags,
                i.getDescription(),
                1,
                createdAt,
                updatedAt,
                lastViewed
        );
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}

