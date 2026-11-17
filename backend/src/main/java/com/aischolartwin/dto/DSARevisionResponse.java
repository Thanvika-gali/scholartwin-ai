package com.aischolartwin.dto;

import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSARevision;
import com.aischolartwin.entity.DSARevisionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DSARevisionResponse(
        Long id,
        Long problemId,
        String problemTitle,
        DSADifficulty difficulty,
        String primaryTopic,
        LocalDate scheduledDate,
        Instant completedAt,
        String revisionNotes,
        DSARevisionStatus status,
        boolean isOverdue,
        Instant createdAt,
        Instant updatedAt
) {
    public static DSARevisionResponse fromEntity(DSARevision revision) {
        boolean overdue = revision.getStatus() == DSARevisionStatus.OVERDUE ||
                (revision.getStatus() == DSARevisionStatus.PENDING && revision.getScheduledDate().isBefore(LocalDate.now()));

        return new DSARevisionResponse(
                revision.getId(),
                revision.getProblem().getId(),
                revision.getProblem().getTitle(),
                revision.getProblem().getDifficulty(),
                revision.getProblem().getPrimaryTopic(),
                revision.getScheduledDate(),
                revision.getCompletedAt(),
                revision.getRevisionNotes(),
                overdue && revision.getStatus() != DSARevisionStatus.COMPLETED ? DSARevisionStatus.OVERDUE : revision.getStatus(),
                overdue && revision.getStatus() != DSARevisionStatus.COMPLETED,
                revision.getCreatedAt(),
                revision.getUpdatedAt()
        );
    }
}

