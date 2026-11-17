package com.aischolartwin.dto;

import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.SkillProficiency;
import com.aischolartwin.entity.SkillSource;

public record CareerSkillRequest(
        String skillName,
        CareerSkillCategory category,
        SkillProficiency proficiency,
        SkillSource source,
        String evidenceText,
        Boolean verified
) {}

