package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.LearningResourceAssociation;
import com.aischolartwin.entity.learning.LearningResourceType;

import java.time.Instant;

public class LearningResourceDtos {

    public record AssociateResourceRequest(
            Long taskId,
            String topic,
            LearningResourceType resourceType,
            Long resourceId,
            String title,
            String url,
            String notes
    ) {}

    public record LearningResourceResponse(
            Long id,
            Long taskId,
            String topic,
            LearningResourceType resourceType,
            Long resourceId,
            String title,
            String url,
            String notes,
            Boolean studied,
            Instant studiedAt,
            Instant createdAt
    ) {
        public static LearningResourceResponse fromEntity(LearningResourceAssociation assoc) {
            return new LearningResourceResponse(
                    assoc.getId(),
                    assoc.getTask() != null ? assoc.getTask().getId() : null,
                    assoc.getTopic(),
                    assoc.getResourceType(),
                    assoc.getResourceId(),
                    assoc.getTitle(),
                    assoc.getUrl(),
                    assoc.getNotes(),
                    assoc.getStudied(),
                    assoc.getStudiedAt(),
                    assoc.getCreatedAt()
            );
        }
    }
}
