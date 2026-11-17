package com.aischolartwin.dto;

import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.SkillProficiency;
import com.aischolartwin.entity.SkillSource;

import java.time.Instant;

public record CareerSkillResponse(
        Long id,
        String skillName,
        CareerSkillCategory category,
        String categoryDisplayName,
        SkillProficiency proficiency,
        SkillSource source,
        String evidenceText,
        boolean verified,
        Instant createdAt,
        Instant updatedAt
) {
    public static CareerSkillResponse fromEntity(CareerSkill skill) {
        return new CareerSkillResponse(
                skill.getId(),
                skill.getSkillName(),
                skill.getCategory(),
                skill.getCategory().getDisplayName(),
                skill.getProficiency(),
                skill.getSource(),
                skill.getEvidenceText(),
                skill.isVerified(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}

