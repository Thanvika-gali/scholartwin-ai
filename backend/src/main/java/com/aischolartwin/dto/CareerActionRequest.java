package com.aischolartwin.dto;

import com.aischolartwin.entity.CareerActionCategory;
import com.aischolartwin.entity.CareerActionPriority;

import java.time.LocalDate;

public record CareerActionRequest(
        String title,
        String description,
        String reasonAndEvidence,
        CareerActionPriority priority,
        CareerActionCategory category,
        String relatedSkillOrRole,
        LocalDate suggestedDeadline
) {}

