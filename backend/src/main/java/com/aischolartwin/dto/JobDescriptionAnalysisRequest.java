package com.aischolartwin.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record JobDescriptionAnalysisRequest(
        String jobTitle,
        @JsonAlias({"company", "companyName"})
        String companyName,
        @JsonAlias({"rawText", "jobDescriptionText", "text", "description"})
        String rawText,
        Long resumeId
) {}

