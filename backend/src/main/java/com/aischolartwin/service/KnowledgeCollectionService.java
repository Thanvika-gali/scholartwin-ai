package com.aischolartwin.service;

import com.aischolartwin.dto.CollectionItemRequest;
import com.aischolartwin.dto.CollectionRequest;
import com.aischolartwin.dto.CollectionResponse;
import com.aischolartwin.dto.ResourceItemResponse;
import com.aischolartwin.entity.KnowledgeCollection;
import com.aischolartwin.entity.KnowledgeCollectionItem;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.KnowledgeCollectionItemRepository;
import com.aischolartwin.repository.KnowledgeCollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeCollectionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeCollectionService.class);

    private final KnowledgeCollectionRepository collectionRepository;
    private final KnowledgeCollectionItemRepository itemRepository;
    private final KnowledgeResourceService resourceService;

    public KnowledgeCollectionService(KnowledgeCollectionRepository collectionRepository,
                                      KnowledgeCollectionItemRepository itemRepository,
                                      KnowledgeResourceService resourceService) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.resourceService = resourceService;
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> listCollections(User user) {
        return collectionRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(c -> toResponse(user, c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollection(User user, Long id) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, id);
        return toResponse(user, collection, true);
    }

    @Transactional
    public CollectionResponse createCollection(User user, CollectionRequest request) {
        KnowledgeCollection collection = new KnowledgeCollection(
                user,
                request.name(),
                request.description(),
                request.color(),
                request.icon()
        );
        collection = collectionRepository.save(collection);
        log.info("Created collection '{}' (id: {}) for user {}", collection.getName(), collection.getId(), user.getEmail());
        return toResponse(user, collection, false);
    }

    @Transactional
    public CollectionResponse updateCollection(User user, Long id, CollectionRequest request) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, id);

        if (request.name() != null && !request.name().isBlank()) {
            collection.setName(request.name());
        }
        if (request.description() != null) {
            collection.setDescription(request.description());
        }
        if (request.color() != null && !request.color().isBlank()) {
            collection.setColor(request.color());
        }
        if (request.icon() != null && !request.icon().isBlank()) {
            collection.setIcon(request.icon());
        }

        collection = collectionRepository.save(collection);
        return toResponse(user, collection, true);
    }

    @Transactional
    public void deleteCollection(User user, Long id) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, id);
        collectionRepository.delete(collection);
        log.info("Deleted collection id: {} for user {}", id, user.getEmail());
    }

    @Transactional
    public CollectionResponse addItemToCollection(User user, Long collectionId, CollectionItemRequest request) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, collectionId);

        // Verify resource exists and belongs to user
        resourceService.verifyResourceOwnership(user, request.resourceType(), request.resourceId());

        if (itemRepository.findByCollectionAndResourceTypeAndResourceId(collection, request.resourceType(), request.resourceId()).isEmpty()) {
            itemRepository.save(new KnowledgeCollectionItem(collection, request.resourceType(), request.resourceId()));
            log.info("Added {} #{} to collection '{}'", request.resourceType(), request.resourceId(), collection.getName());
        }
        return toResponse(user, collection, true);
    }

    @Transactional
    public CollectionResponse removeItemFromCollection(User user, Long collectionId, ResourceType type, Long resourceId) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, collectionId);
        itemRepository.deleteByCollectionAndResourceTypeAndResourceId(collection, type, resourceId);
        return toResponse(user, collection, true);
    }

    public KnowledgeCollection findUserCollectionOrThrow(User user, Long id) {
        return collectionRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found or access denied (id: " + id + ")"));
    }

    @Transactional(readOnly = true)
    public List<ResourceItemResponse> getCollectionItems(User user, Long id) {
        KnowledgeCollection collection = findUserCollectionOrThrow(user, id);
        List<KnowledgeCollectionItem> collectionItems = itemRepository.findByCollectionOrderByAddedAtDesc(collection);
        List<ResourceItemResponse> items = new ArrayList<>();
        for (KnowledgeCollectionItem item : collectionItems) {
            try {
                ResourceItemResponse r = resourceService.getResourceItem(user, item.getResourceType(), item.getResourceId());
                if (r != null) items.add(r);
            } catch (Exception ignored) {}
        }
        return items;
    }

    private CollectionResponse toResponse(User user, KnowledgeCollection c, boolean populateItems) {
        long count = itemRepository.countByCollection(c);
        List<ResourceItemResponse> items = new ArrayList<>();

        if (populateItems) {
            List<KnowledgeCollectionItem> collectionItems = itemRepository.findByCollectionOrderByAddedAtDesc(c);
            for (KnowledgeCollectionItem item : collectionItems) {
                try {
                    ResourceItemResponse r = resourceService.getResourceItem(user, item.getResourceType(), item.getResourceId());
                    if (r != null) items.add(r);
                } catch (Exception ignored) {}
            }
        }

        return new CollectionResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getColor(),
                c.getIcon(),
                count,
                items,
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
