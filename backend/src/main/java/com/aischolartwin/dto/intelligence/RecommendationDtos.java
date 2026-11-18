package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.AIRecommendation;
import com.aischolartwin.entity.intelligence.RecommendationCategory;
import com.aischolartwin.entity.intelligence.RecommendationPriority;
import com.aischolartwin.entity.intelligence.RecommendationStatus;

import java.time.Instant;

public class RecommendationDtos {

    public record RecommendationResponse(
            Long id,
            String title,
            RecommendationCategory category,
            RecommendationPriority priority,
            String reason,
            String supportingEvidence,
            String actionToTake,
            String actionUrl,
            RecommendationStatus status,
            Instant completedAt,
            Instant dismissedAt,
            Instant createdAt
    ) {
        public static RecommendationResponse fromEntity(AIRecommendation r) {
            return new RecommendationResponse(
                    r.getId(),
                    r.getTitle(),
                    r.getCategory(),
                    r.getPriority(),
                    r.getReason(),
                    r.getSupportingEvidence(),
                    r.getActionToTake(),
                    r.getActionUrl(),
                    r.getStatus(),
                    r.getCompletedAt(),
                    r.getDismissedAt(),
                    r.getCreatedAt()
            );
        }
    }
}
