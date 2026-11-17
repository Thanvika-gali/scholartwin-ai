package com.aischolartwin.service;

import com.aischolartwin.dto.ResourceItemResponse;
import com.aischolartwin.dto.ResourceRelationRequest;
import com.aischolartwin.dto.ResourceRelationResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.KnowledgeResourceRelationRepository;
import com.aischolartwin.repository.KnowledgeResourceTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KnowledgeRelationService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRelationService.class);

    private final KnowledgeResourceRelationRepository relationRepository;
    private final KnowledgeResourceTagRepository resourceTagRepository;
    private final KnowledgeResourceService resourceService;

    public KnowledgeRelationService(KnowledgeResourceRelationRepository relationRepository,
                                    KnowledgeResourceTagRepository resourceTagRepository,
                                    KnowledgeResourceService resourceService) {
        this.relationRepository = relationRepository;
        this.resourceTagRepository = resourceTagRepository;
        this.resourceService = resourceService;
    }

    @Transactional(readOnly = true)
    public List<ResourceRelationResponse> getRelatedResources(User user, ResourceType type, Long id) {
        resourceService.verifyResourceOwnership(user, type, id);
        Map<String, ResourceRelationResponse> map = new LinkedHashMap<>();

        // 1. Fetch manual relationships
        List<KnowledgeResourceRelation> direct = relationRepository.findAllRelationsForResource(user, type, id);
        for (KnowledgeResourceRelation r : direct) {
            boolean isSource = r.getSourceType() == type && r.getSourceId().equals(id);
            ResourceType targetType = isSource ? r.getTargetType() : r.getSourceType();
            Long targetId = isSource ? r.getTargetId() : r.getSourceId();

            try {
                ResourceItemResponse target = resourceService.getResourceItem(user, targetType, targetId);
                String key = targetType.name() + ":" + targetId;
                map.put(key, new ResourceRelationResponse(
                        r.getId(),
                        targetType,
                        targetId,
                        target.title(),
                        r.getRelationType(),
                        r.getReason() != null ? r.getReason() : "Directly connected reference",
                        true,
                        r.getCreatedAt()
                ));
            } catch (Exception ignored) {}
        }

        // 2. Fetch resources with shared tags
        List<KnowledgeTag> tags = resourceTagRepository.findTagsByResource(type, id);
        if (!tags.isEmpty()) {
            List<KnowledgeResourceTag> shared = resourceTagRepository.findSharedTagResources(tags, type, id);
            for (KnowledgeResourceTag s : shared) {
                String key = s.getResourceType().name() + ":" + s.getResourceId();
                if (!map.containsKey(key)) {
                    try {
                        ResourceItemResponse target = resourceService.getResourceItem(user, s.getResourceType(), s.getResourceId());
                        map.put(key, new ResourceRelationResponse(
                                null,
                                s.getResourceType(),
                                s.getResourceId(),
                                target.title(),
                                "SHARED_TOPIC",
                                "Shared topic / tag: #" + s.getTag().getName(),
                                false,
                                null
                        ));
                    } catch (Exception ignored) {}
                }
            }
        }

        return new ArrayList<>(map.values());
    }

    @Transactional
    public ResourceRelationResponse connectResources(User user, ResourceType sourceType, Long sourceId, ResourceRelationRequest request) {
        if (sourceType == request.targetType() && sourceId.equals(request.targetId())) {
            throw new InvalidPayloadException("Cannot connect a resource to itself.");
        }

        resourceService.verifyResourceOwnership(user, sourceType, sourceId);
        resourceService.verifyResourceOwnership(user, request.targetType(), request.targetId());

        Optional<KnowledgeResourceRelation> existing = relationRepository
                .findByUserAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(user, sourceType, sourceId, request.targetType(), request.targetId());

        KnowledgeResourceRelation rel = existing.orElseGet(() -> new KnowledgeResourceRelation(
                user, sourceType, sourceId, request.targetType(), request.targetId(),
                request.relationType(), request.reason(), true
        ));

        if (request.relationType() != null) rel.setRelationType(request.relationType());
        if (request.reason() != null) rel.setReason(request.reason());

        rel = relationRepository.save(rel);
        ResourceItemResponse target = resourceService.getResourceItem(user, request.targetType(), request.targetId());

        log.info("Connected {} #{} to {} #{}", sourceType, sourceId, request.targetType(), request.targetId());
        return new ResourceRelationResponse(
                rel.getId(),
                rel.getTargetType(),
                rel.getTargetId(),
                target.title(),
                rel.getRelationType(),
                rel.getReason(),
                true,
                rel.getCreatedAt()
        );
    }

    @Transactional
    public void disconnectResources(User user, ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId) {
        relationRepository.deleteByUserAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(user, sourceType, sourceId, targetType, targetId);
        // Also check reverse direction
        relationRepository.deleteByUserAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(user, targetType, targetId, sourceType, sourceId);
        log.info("Disconnected relationship between {} #{} and {} #{}", sourceType, sourceId, targetType, targetId);
    }
}

