package com.aischolartwin.dto;

public record DuplicateCheckResponse(
        boolean isDuplicate,
        String fileHash,
        Long existingDocumentId,
        String existingDocumentTitle,
        Long existingFolderId
) {}

