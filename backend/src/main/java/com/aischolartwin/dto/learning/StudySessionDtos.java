package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.StudySession;

import java.time.Instant;
import java.time.LocalDate;

public class StudySessionDtos {

    public record LogStudySessionRequest(
            LocalDate sessionDate,
            Integer durationMinutes,
            String category,
            String title,
            String notes,
            Long linkedRoadmapId,
            Long linkedTaskId
    ) {}

    public record StudySessionResponse(
            Long id,
            LocalDate sessionDate,
            Integer durationMinutes,
            String category,
            String title,
            String notes,
            Long linkedRoadmapId,
            Long linkedTaskId,
            Instant createdAt
    ) {
        public static StudySessionResponse fromEntity(StudySession session) {
            return new StudySessionResponse(
                    session.getId(),
                    session.getSessionDate(),
                    session.getDurationMinutes(),
                    session.getCategory(),
                    session.getTitle(),
                    session.getNotes(),
                    session.getLinkedRoadmapId(),
                    session.getLinkedTaskId(),
                    session.getCreatedAt()
            );
        }
    }
}
