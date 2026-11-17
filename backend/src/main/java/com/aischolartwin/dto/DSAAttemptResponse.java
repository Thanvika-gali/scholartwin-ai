package com.aischolartwin.dto;

import com.aischolartwin.entity.DSAAttempt;
import com.aischolartwin.entity.DSAAttemptOutcome;

import java.time.Instant;
import java.time.LocalDate;

public record DSAAttemptResponse(
        Long id,
        Long problemId,
        String problemTitle,
        LocalDate attemptDate,
        DSAAttemptOutcome outcome,
        Integer timeSpentMinutes,
        String programmingLanguage,
        String learningNotes,
        String difficultyEncountered,
        Instant createdAt
) {
    public static DSAAttemptResponse fromEntity(DSAAttempt attempt) {
        return new DSAAttemptResponse(
                attempt.getId(),
                attempt.getProblem().getId(),
                attempt.getProblem().getTitle(),
                attempt.getAttemptDate(),
                attempt.getOutcome(),
                attempt.getTimeSpentMinutes(),
                attempt.getProgrammingLanguage(),
                attempt.getLearningNotes(),
                attempt.getDifficultyEncountered(),
                attempt.getCreatedAt()
        );
    }
}

