package com.aischolartwin.dto;

public record ResumeDetailResponse(
        ResumeResponse resume,
        ExtractedResumeDataDto extractedData,
        String rawText,
        ResumeAnalysisResponse latestAnalysis,
        long totalAnalysesCount
) {}

