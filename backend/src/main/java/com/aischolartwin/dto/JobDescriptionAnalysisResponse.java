package com.aischolartwin.dto;

import com.aischolartwin.entity.JobDescriptionAnalysis;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record JobDescriptionAnalysisResponse(
        Long id,
        String jobTitle,
        String companyName,
        Long resumeId,
        String resumeTitle,
        String targetRole,
        String experienceRequired,
        String educationRequired,
        List<String> responsibilities,
        double matchPercentage,
        List<String> matchedRequiredSkills,
        List<String> missingRequiredSkills,
        List<String> matchedPreferredSkills,
        List<String> missingPreferredSkills,
        List<String> keywordCoverage,
        List<String> tailoredSuggestions,
        Instant createdAt
) {
    @JsonProperty("matchScore")
    public double matchScore() {
        return matchPercentage;
    }

    @JsonProperty("company")
    public String company() {
        return companyName;
    }

    @JsonProperty("missingKeywords")
    public List<String> missingKeywords() {
        return missingRequiredSkills != null ? missingRequiredSkills : List.of();
    }

    @JsonProperty("recommendations")
    public List<String> recommendations() {
        return tailoredSuggestions != null ? tailoredSuggestions : List.of();
    }

    public static JobDescriptionAnalysisResponse fromEntity(
            JobDescriptionAnalysis analysis,
            List<String> responsibilities,
            List<String> matchedRequired,
            List<String> missingRequired,
            List<String> matchedPreferred,
            List<String> missingPreferred,
            List<String> keywords,
            List<String> suggestions
    ) {
        return new JobDescriptionAnalysisResponse(
                analysis.getId(),
                analysis.getJobTitle(),
                analysis.getCompanyName(),
                analysis.getResume() != null ? analysis.getResume().getId() : null,
                analysis.getResume() != null ? analysis.getResume().getTitle() : null,
                analysis.getTargetRole(),
                analysis.getExperienceRequired(),
                analysis.getEducationRequired(),
                responsibilities,
                analysis.getMatchPercentage(),
                matchedRequired,
                missingRequired,
                matchedPreferred,
                missingPreferred,
                keywords,
                suggestions,
                analysis.getCreatedAt()
        );
    }
}

