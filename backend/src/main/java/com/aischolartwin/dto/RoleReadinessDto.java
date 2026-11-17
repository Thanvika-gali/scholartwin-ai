package com.aischolartwin.dto;

import java.util.List;

public record RoleReadinessDto(
        String roleId,
        String roleName,
        double readinessPercentage,
        int requiredMatchedCount,
        int requiredTotalCount,
        int preferredMatchedCount,
        int preferredTotalCount,
        List<String> matchedRequiredSkills,
        List<String> missingRequiredSkills,
        List<String> matchedPreferredSkills,
        List<String> missingPreferredSkills,
        List<String> recommendedSkillsToLearn,
        List<String> supportingEvidence,
        String dsaSupportingEvidence,
        List<String> resumeSkills,
        List<String> verifiedSkills,
        List<String> inferredSkills,
        List<String> missingSkills
) {
    public String algorithmicEvidence() {
        return dsaSupportingEvidence;
    }
}

