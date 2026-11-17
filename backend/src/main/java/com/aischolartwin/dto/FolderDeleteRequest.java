package com.aischolartwin.dto;

public record FolderDeleteRequest(
        String reassignOption, // "INBOX", "PARENT", "TARGET_FOLDER"
        Long targetFolderId
) {}

