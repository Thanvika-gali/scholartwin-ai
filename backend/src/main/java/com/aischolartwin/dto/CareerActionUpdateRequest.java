package com.aischolartwin.dto;

import com.aischolartwin.entity.CareerActionCategory;
import com.aischolartwin.entity.CareerActionPriority;
import com.aischolartwin.entity.CareerActionStatus;

import java.time.LocalDate;

public record CareerActionUpdateRequest(
        String title,
        String description,
        String reasonAndEvidence,
        CareerActionPriority priority,
        CareerActionCategory category,
        String relatedSkillOrRole,
        CareerActionStatus status,
        LocalDate suggestedDeadline
) {}

