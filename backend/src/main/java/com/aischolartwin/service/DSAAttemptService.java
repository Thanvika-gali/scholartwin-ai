package com.aischolartwin.service;

import com.aischolartwin.dto.DSAAttemptRequest;
import com.aischolartwin.dto.DSAAttemptResponse;
import com.aischolartwin.dto.DSAAttemptStatsResponse;
import com.aischolartwin.entity.DSAAttempt;
import com.aischolartwin.entity.DSAAttemptOutcome;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class DSAAttemptService {

    private final DSAAttemptRepository attemptRepository;
    private final DSAProblemRepository problemRepository;
    private final DSAProblemService problemService;

    public DSAAttemptService(
            DSAAttemptRepository attemptRepository,
            DSAProblemRepository problemRepository,
            DSAProblemService problemService
    ) {
        this.attemptRepository = attemptRepository;
        this.problemRepository = problemRepository;
        this.problemService = problemService;
    }

    @Transactional
    public DSAAttemptResponse logAttempt(User user, Long problemId, DSAAttemptRequest request) {
        if (request == null) {
            throw new InvalidPayloadException("Attempt payload cannot be null.");
        }
        if (request.outcome() == null) {
            throw new InvalidPayloadException("Attempt outcome (SUCCESS or FAILED) is required.");
        }
        if (request.timeSpentMinutes() != null && request.timeSpentMinutes() < 0) {
            throw new InvalidPayloadException("Time spent cannot be negative.");
        }

        DSAProblem problem = problemService.getProblemEntity(user, problemId);

        LocalDate date = request.attemptDate() != null ? request.attemptDate() : LocalDate.now();

        DSAAttempt attempt = new DSAAttempt(
                problem,
                user,
                date,
                request.outcome(),
                request.timeSpentMinutes(),
                request.programmingLanguage(),
                request.learningNotes(),
                request.difficultyEncountered()
        );

        DSAAttempt saved = attemptRepository.save(attempt);

        // Update problem status appropriately
        if (request.outcome() == DSAAttemptOutcome.SUCCESS) {
            if (problem.getStatus() != DSAStatus.SOLVED) {
                problem.setStatus(DSAStatus.SOLVED);
                problem.setSolvedAt(Instant.now());
                problemRepository.save(problem);
            }
        } else if (request.outcome() == DSAAttemptOutcome.FAILED) {
            if (problem.getStatus() == DSAStatus.NOT_STARTED) {
                problem.setStatus(DSAStatus.ATTEMPTED);
                problemRepository.save(problem);
            }
        }

        return DSAAttemptResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<DSAAttemptResponse> getAttemptsForProblem(User user, Long problemId) {
        DSAProblem problem = problemService.getProblemEntity(user, problemId);
        return attemptRepository.findByProblemOrderByAttemptDateDescCreatedAtDesc(problem)
                .stream()
                .map(DSAAttemptResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DSAAttemptStatsResponse getProblemStats(User user, Long problemId) {
        DSAProblem problem = problemService.getProblemEntity(user, problemId);
        long totalAttempts = attemptRepository.countByProblem(problem);
        long successAttempts = attemptRepository.countByProblemAndOutcome(problem, DSAAttemptOutcome.SUCCESS);
        long failedAttempts = totalAttempts - successAttempts;
        double successRate = totalAttempts > 0 ? ((double) successAttempts / totalAttempts) * 100.0 : 0.0;
        Double avgTime = attemptRepository.findAverageTimeSpentByProblem(problem);

        return new DSAAttemptStatsResponse(
                totalAttempts,
                successAttempts,
                failedAttempts,
                Math.round(successRate * 10.0) / 10.0,
                avgTime != null ? Math.round(avgTime * 10.0) / 10.0 : null
        );
    }
}

