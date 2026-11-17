package com.aischolartwin.dto;

import com.aischolartwin.entity.CareerAction;
import com.aischolartwin.entity.CareerActionCategory;
import com.aischolartwin.entity.CareerActionPriority;
import com.aischolartwin.entity.CareerActionStatus;

import java.time.Instant;
import java.time.LocalDate;

public record CareerActionResponse(
        Long id,
        String title,
        String description,
        String reasonAndEvidence,
        CareerActionPriority priority,
        CareerActionCategory category,
        String relatedSkillOrRole,
        CareerActionStatus status,
        LocalDate suggestedDeadline,
        Instant completedAt,
        boolean isOverdue,
        Instant createdAt,
        Instant updatedAt
) {
    public static CareerActionResponse fromEntity(CareerAction action) {
        boolean overdue = action.getStatus() != CareerActionStatus.COMPLETED
                && action.getSuggestedDeadline() != null
                && action.getSuggestedDeadline().isBefore(LocalDate.now());

        return new CareerActionResponse(
                action.getId(),
                action.getTitle(),
                action.getDescription(),
                action.getReasonAndEvidence(),
                action.getPriority(),
                action.getCategory(),
                action.getRelatedSkillOrRole(),
                action.getStatus(),
                action.getSuggestedDeadline(),
                action.getCompletedAt(),
                overdue,
                action.getCreatedAt(),
                action.getUpdatedAt()
        );
    }
}

