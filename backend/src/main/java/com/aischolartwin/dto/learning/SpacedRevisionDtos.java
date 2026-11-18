package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.RevisionAttempt;
import com.aischolartwin.entity.learning.RevisionItemType;
import com.aischolartwin.entity.learning.RevisionRating;
import com.aischolartwin.entity.learning.RevisionStatus;
import com.aischolartwin.entity.learning.SpacedRevisionItem;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SpacedRevisionDtos {

    public record CreateRevisionItemRequest(
            String title,
            RevisionItemType itemType,
            Long linkedReferenceId,
            LocalDate scheduledDate,
            String customIntervalSequence,
            String notes
    ) {}

    public record CompleteRevisionRequest(
            RevisionRating rating,
            String reflectionNotes
    ) {}

    public record RevisionAttemptResponse(
            Long id,
            RevisionRating rating,
            Instant attemptedAt,
            Integer previousIntervalDays,
            Integer newIntervalDays,
            Integer previousStage,
            Integer newStage,
            LocalDate nextScheduledDate,
            String reflectionNotes
    ) {
        public static RevisionAttemptResponse fromEntity(RevisionAttempt attempt) {
            return new RevisionAttemptResponse(
                    attempt.getId(),
                    attempt.getRating(),
                    attempt.getAttemptedAt(),
                    attempt.getPreviousIntervalDays(),
                    attempt.getNewIntervalDays(),
                    attempt.getPreviousStage(),
                    attempt.getNewStage(),
                    attempt.getNextScheduledDate(),
                    attempt.getReflectionNotes()
            );
        }
    }

    public record SpacedRevisionResponse(
            Long id,
            String title,
            RevisionItemType itemType,
            Long linkedReferenceId,
            Integer stage,
            Integer intervalDays,
            String customIntervalSequence,
            LocalDate scheduledDate,
            Instant lastReviewedAt,
            RevisionStatus status,
            RevisionRating lastRating,
            Integer consecutiveSuccessCount,
            String notes,
            List<RevisionAttemptResponse> attempts,
            Instant createdAt
    ) {
        public static SpacedRevisionResponse fromEntity(SpacedRevisionItem item) {
            List<RevisionAttemptResponse> attemptDtos = item.getAttempts() != null
                    ? item.getAttempts().stream().map(RevisionAttemptResponse::fromEntity).toList()
                    : List.of();
            return new SpacedRevisionResponse(
                    item.getId(),
                    item.getTitle(),
                    item.getItemType(),
                    item.getLinkedReferenceId(),
                    item.getStage(),
                    item.getIntervalDays(),
                    item.getCustomIntervalSequence(),
                    item.getScheduledDate(),
                    item.getLastReviewedAt(),
                    item.getStatus(),
                    item.getLastRating(),
                    item.getConsecutiveSuccessCount(),
                    item.getNotes(),
                    attemptDtos,
                    item.getCreatedAt()
            );
        }
    }

    public record RevisionQueueSummaryResponse(
            int dueTodayCount,
            int overdueCount,
            int upcomingCount,
            int completedCount,
            int needsAttentionCount,
            List<SpacedRevisionResponse> dueToday,
            List<SpacedRevisionResponse> overdue,
            List<SpacedRevisionResponse> upcoming,
            List<SpacedRevisionResponse> completed
    ) {}
}
