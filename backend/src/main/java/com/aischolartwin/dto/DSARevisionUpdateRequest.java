package com.aischolartwin.dto;

import com.aischolartwin.entity.DSARevisionStatus;

import java.time.LocalDate;

public record DSARevisionUpdateRequest(
        LocalDate scheduledDate,
        String revisionNotes,
        DSARevisionStatus status
) {
}

