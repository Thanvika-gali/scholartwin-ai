package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class MemoryDtos {

    public record CreateMemoryRequest(
            @NotNull(message = "Memory category is required")
            MemoryCategory category,
            @Size(max = 255)
            String summary,
            @NotBlank(message = "Memory content cannot be blank")
            String content,
            MemoryType memoryType,
            MemoryProvenance provenance,
            MemorySourceType sourceType,
            String sourceReference,
            Double confidence,
            MemoryImportance importance
    ) {}

    public record UpdateMemoryRequest(
            MemoryCategory category,
            String summary,
            String content,
            MemoryType memoryType,
            MemoryImportance importance,
            MemoryStatus status,
            Boolean isPinned
    ) {}

    public record BulkDeleteRequest(
            List<Long> memoryIds,
            Boolean clearAll
    ) {}

    public record MemoryResponse(
            Long id,
            MemoryCategory category,
            String summary,
            String content,
            MemoryType memoryType,
            MemoryProvenance provenance,
            MemorySourceType sourceType,
            String sourceReference,
            Double confidence,
            MemoryImportance importance,
            MemoryStatus status,
            Boolean isPinned,
            Instant lastAccessedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static MemoryResponse fromEntity(UserMemory m) {
            return new MemoryResponse(
                    m.getId(),
                    m.getCategory(),
                    m.getSummary(),
                    m.getContent(),
                    m.getMemoryType(),
                    m.getProvenance(),
                    m.getSourceType(),
                    m.getSourceReference(),
                    m.getConfidence(),
                    m.getImportance(),
                    m.getStatus(),
                    m.getIsPinned(),
                    m.getLastAccessedAt(),
                    m.getCreatedAt(),
                    m.getUpdatedAt()
            );
        }
    }

    public record MemoryExtractionResult(
            int extractedCount,
            int consolidatedCount,
            List<String> sampleSummaries
    ) {}

    public record MemoryStatsResponse(
            long totalMemories,
            long activeMemories,
            long archivedMemories,
            long pinnedMemories
    ) {}
}
