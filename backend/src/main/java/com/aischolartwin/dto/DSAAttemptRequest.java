package com.aischolartwin.dto;

import com.aischolartwin.entity.DSAAttemptOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DSAAttemptRequest(
        LocalDate attemptDate,

        @NotNull(message = "Attempt outcome (SUCCESS or FAILED) is required.")
        DSAAttemptOutcome outcome,

        @PositiveOrZero(message = "Time spent must be 0 or positive minutes.")
        Integer timeSpentMinutes,

        @Size(max = 50, message = "Language cannot exceed 50 characters.")
        String programmingLanguage,

        String learningNotes,

        @Size(max = 255, message = "Difficulty notes cannot exceed 255 characters.")
        String difficultyEncountered
) {
}

