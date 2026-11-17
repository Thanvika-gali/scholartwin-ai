package com.aischolartwin.dto;

import java.util.List;

public record SkillGapAnalysisResponse(
        RoleReadinessDto targetRoleReadiness,
        List<RoleReadinessDto> allRoles,
        long totalVerifiedSkillsCount,
        List<String> topMissingSkillsAcrossRoles
) {}

