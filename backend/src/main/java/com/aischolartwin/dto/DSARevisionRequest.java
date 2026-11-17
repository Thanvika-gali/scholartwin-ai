package com.aischolartwin.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DSARevisionRequest(
        @NotNull(message = "Scheduled revision date is required.")
        LocalDate scheduledDate,

        String revisionNotes
) {
}

