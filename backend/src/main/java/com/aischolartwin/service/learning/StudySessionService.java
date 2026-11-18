package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.StudySessionDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.StudySession;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.learning.StudySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudySessionService {

    private final StudySessionRepository sessionRepository;

    public StudySessionService(StudySessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<StudySessionDtos.StudySessionResponse> getSessions(User user) {
        List<StudySession> sessions = sessionRepository.findByUserOrderBySessionDateDescCreatedAtDesc(user);
        return sessions.stream().map(StudySessionDtos.StudySessionResponse::fromEntity).toList();
    }

    @Transactional
    public StudySessionDtos.StudySessionResponse logSession(User user, StudySessionDtos.LogStudySessionRequest request) {
        if (request.durationMinutes() == null || request.durationMinutes() <= 0) {
            throw new InvalidPayloadException("Session duration must be greater than zero minutes.");
        }

        LocalDate sessionDate = request.sessionDate() != null ? request.sessionDate() : LocalDate.now();
        String title = request.title() != null && !request.title().isBlank()
                ? request.title().trim()
                : "Study Session (" + (request.category() != null ? request.category() : "General") + ")";

        StudySession session = new StudySession(
                user,
                sessionDate,
                request.durationMinutes(),
                request.category() != null ? request.category() : "ROADMAP_STUDY",
                title,
                request.notes()
        );
        session.setLinkedRoadmapId(request.linkedRoadmapId());
        session.setLinkedTaskId(request.linkedTaskId());

        StudySession saved = sessionRepository.save(session);
        return StudySessionDtos.StudySessionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public long getTotalStudyMinutes(User user) {
        return sessionRepository.sumTotalMinutesByUser(user);
    }

    @Transactional(readOnly = true)
    public int calculateCurrentStreak(User user) {
        List<LocalDate> distinctDates = sessionRepository.findDistinctSessionDatesByUser(user);
        if (distinctDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Streak is only active if user studied today or yesterday
        LocalDate firstDate = distinctDates.get(0);
        if (!firstDate.isEqual(today) && !firstDate.isEqual(yesterday)) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = firstDate;

        for (LocalDate date : distinctDates) {
            if (date.isEqual(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
