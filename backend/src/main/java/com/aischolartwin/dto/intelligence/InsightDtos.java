package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.*;

import java.time.Instant;
import java.util.List;

public class InsightDtos {

    public record InsightEvidenceResponse(
            Long id,
            String evidenceType,
            String description,
            String sourceModule,
            String metricValue,
            Instant recordedAt
    ) {
        public static InsightEvidenceResponse fromEntity(InsightEvidence e) {
            return new InsightEvidenceResponse(
                    e.getId(),
                    e.getEvidenceType(),
                    e.getDescription(),
                    e.getSourceModule(),
                    e.getMetricValue(),
                    e.getRecordedAt()
            );
        }
    }

    public record InsightResponse(
            Long id,
            String title,
            String description,
            InsightCategory category,
            InsightImportance importance,
            Double confidence,
            String recommendedAction,
            String actionModule,
            String actionPayloadJson,
            InsightStatus status,
            Instant generatedAt,
            List<InsightEvidenceResponse> evidenceList
    ) {
        public static InsightResponse fromEntity(Insight i) {
            List<InsightEvidenceResponse> evidences = (i.getEvidenceList() != null)
                    ? i.getEvidenceList().stream().map(InsightEvidenceResponse::fromEntity).toList()
                    : List.of();
            return new InsightResponse(
                    i.getId(),
                    i.getTitle(),
                    i.getDescription(),
                    i.getCategory(),
                    i.getImportance(),
                    i.getConfidence(),
                    i.getRecommendedAction(),
                    i.getActionModule(),
                    i.getActionPayloadJson(),
                    i.getStatus(),
                    i.getGeneratedAt(),
                    evidences
            );
        }
    }

    public record GenerateInsightsRequest(
            Boolean forceRefresh,
            List<InsightCategory> preferredCategories
    ) {}

    public record GenerateInsightsResponse(
            int insightsCount,
            Instant refreshedAt,
            List<InsightResponse> insights
    ) {}
}
