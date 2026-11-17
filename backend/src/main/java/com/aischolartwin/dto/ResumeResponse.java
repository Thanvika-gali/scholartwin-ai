package com.aischolartwin.dto;

import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.ResumeFileType;
import com.aischolartwin.entity.ResumeProcessingStatus;

import java.time.Instant;

public record ResumeResponse(
        Long id,
        String title,
        String originalFilename,
        ResumeFileType fileType,
        Long fileSizeBytes,
        int versionNumber,
        boolean isPrimary,
        ResumeProcessingStatus processingStatus,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        boolean hasAnalysis,
        Integer latestScore
) {
    public static ResumeResponse fromEntity(Resume resume, boolean hasAnalysis, Integer latestScore) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getOriginalFilename(),
                resume.getFileType(),
                resume.getFileSizeBytes(),
                resume.getVersionNumber(),
                resume.isPrimary(),
                resume.getProcessingStatus(),
                resume.getErrorMessage(),
                resume.getCreatedAt(),
                resume.getUpdatedAt(),
                hasAnalysis,
                latestScore
        );
    }
}

