package com.aischolartwin.service;

import com.aischolartwin.dto.DSAAttemptStatsResponse;
import com.aischolartwin.dto.DSAProblemDetailResponse;
import com.aischolartwin.dto.DSAProblemRequest;
import com.aischolartwin.dto.DSAProblemResponse;
import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DSAProblemService {

    private final DSAProblemRepository problemRepository;
    private final DSAAttemptRepository attemptRepository;
    private final DSARevisionRepository revisionRepository;

    public DSAProblemService(
            DSAProblemRepository problemRepository,
            DSAAttemptRepository attemptRepository,
            DSARevisionRepository revisionRepository) {
        this.problemRepository = problemRepository;
        this.attemptRepository = attemptRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional
    public DSAProblemResponse createProblem(User user, DSAProblemRequest request) {
        validateRequest(request);

        DSAProblem problem = new DSAProblem(
                user,
                request.title().trim(),
                cleanUrl(request.problemUrl()),
                request.platform(),
                request.difficulty(),
                request.primaryTopic().trim());

        if (request.additionalTopics() != null) {
            problem.setAdditionalTopics(
                    request.additionalTopics().stream()
                            .filter(t -> t != null && !t.isBlank())
                            .map(String::trim)
                            .collect(Collectors.toSet()));
        }

        if (request.status() != null) {
            problem.setStatus(request.status());
            if (request.status() == DSAStatus.SOLVED) {
                problem.setSolvedAt(Instant.now());
            }
        }

        problem.setPersonalNotes(request.personalNotes());
        problem.setSolutionApproach(request.solutionApproach());
        problem.setTimeComplexity(request.timeComplexity());
        problem.setSpaceComplexity(request.spaceComplexity());

        if (Boolean.TRUE.equals(request.revisionRequired())) {
            problem.setRevisionRequired(true);
        }

        DSAProblem saved = problemRepository.save(problem);
        return DSAProblemResponse.fromEntity(saved, 0, 0.0);
    }

    @Transactional(readOnly = true)
    public DSAProblem getProblemEntity(User user, Long problemId) {
        return problemRepository.findByIdAndUser(problemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("DSA Problem not found with ID: " + problemId));
    }

    @Transactional(readOnly = true)
    public DSAProblemResponse getProblem(User user, Long problemId) {
        DSAProblem problem = getProblemEntity(user, problemId);
        long totalAttempts = attemptRepository.countByProblem(problem);
        long successAttempts = attemptRepository.countByProblemAndOutcome(problem,
                com.aischolartwin.entity.DSAAttemptOutcome.SUCCESS);
        double successRate = totalAttempts > 0 ? ((double) successAttempts / totalAttempts) * 100.0 : 0.0;

        return DSAProblemResponse.fromEntity(problem, (int) totalAttempts, successRate);
    }

    @Transactional(readOnly = true)
    public DSAProblemDetailResponse getProblemDetail(User user, Long problemId) {
        DSAProblem problem = getProblemEntity(user, problemId);

        var attempts = attemptRepository.findByProblemOrderByAttemptDateDescCreatedAtDesc(problem)
                .stream()
                .map(com.aischolartwin.dto.DSAAttemptResponse::fromEntity)
                .toList();

        var revisions = revisionRepository.findByProblemOrderByScheduledDateDesc(problem)
                .stream()
                .map(com.aischolartwin.dto.DSARevisionResponse::fromEntity)
                .toList();

        long totalAttempts = attempts.size();
        long successAttempts = attempts.stream()
                .filter(a -> a.outcome() == com.aischolartwin.entity.DSAAttemptOutcome.SUCCESS)
                .count();
        long failedAttempts = totalAttempts - successAttempts;
        double successRate = totalAttempts > 0 ? ((double) successAttempts / totalAttempts) * 100.0 : 0.0;
        Double avgTime = attemptRepository.findAverageTimeSpentByProblem(problem);

        DSAAttemptStatsResponse stats = new DSAAttemptStatsResponse(
                totalAttempts,
                successAttempts,
                failedAttempts,
                Math.round(successRate * 10.0) / 10.0,
                avgTime != null ? Math.round(avgTime * 10.0) / 10.0 : null);

        DSAProblemResponse problemResponse = DSAProblemResponse.fromEntity(problem, (int) totalAttempts,
                stats.successRatePercent());

        return new DSAProblemDetailResponse(problemResponse, attempts, revisions, stats);
    }

    @Transactional
    public DSAProblemResponse updateProblem(User user, Long problemId, DSAProblemRequest request) {
        validateRequest(request);

        DSAProblem problem = getProblemEntity(user, problemId);

        problem.setTitle(request.title().trim());
        problem.setProblemUrl(cleanUrl(request.problemUrl()));
        if (request.platform() != null && !request.platform().isBlank()) {
            problem.setPlatform(request.platform().trim());
        }
        if (request.difficulty() != null) {
            problem.setDifficulty(request.difficulty());
        }
        problem.setPrimaryTopic(request.primaryTopic().trim());

        if (request.additionalTopics() != null) {
            problem.setAdditionalTopics(
                    request.additionalTopics().stream()
                            .filter(t -> t != null && !t.isBlank())
                            .map(String::trim)
                            .collect(Collectors.toSet()));
        }

        if (request.status() != null) {
            if (problem.getStatus() != DSAStatus.SOLVED && request.status() == DSAStatus.SOLVED) {
                problem.setSolvedAt(Instant.now());
            } else if (request.status() != DSAStatus.SOLVED) {
                problem.setSolvedAt(null);
            }
            problem.setStatus(request.status());
        }

        problem.setPersonalNotes(request.personalNotes());
        problem.setSolutionApproach(request.solutionApproach());
        problem.setTimeComplexity(request.timeComplexity());
        problem.setSpaceComplexity(request.spaceComplexity());

        if (request.revisionRequired() != null) {
            problem.setRevisionRequired(request.revisionRequired());
        }

        DSAProblem updated = problemRepository.save(problem);
        long totalAttempts = attemptRepository.countByProblem(updated);
        long successAttempts = attemptRepository.countByProblemAndOutcome(updated,
                com.aischolartwin.entity.DSAAttemptOutcome.SUCCESS);
        double successRate = totalAttempts > 0 ? ((double) successAttempts / totalAttempts) * 100.0 : 0.0;

        return DSAProblemResponse.fromEntity(updated, (int) totalAttempts, successRate);
    }

    @Transactional
    public void deleteProblem(User user, Long problemId) {
        DSAProblem problem = getProblemEntity(user, problemId);
        revisionRepository.deleteByProblem(problem);
        attemptRepository.deleteByProblem(problem);
        problemRepository.delete(problem);
    }

    @Transactional(readOnly = true)
    public Page<DSAProblemResponse> searchProblems(
            User user,
            String search,
            String topic,
            DSADifficulty difficulty,
            String platform,
            DSAStatus status,
            Boolean revisionRequired,
            Pageable pageable) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        String topicParam = (topic != null && !topic.isBlank()) ? topic.trim() : null;
        String platformParam = (platform != null && !platform.isBlank()) ? platform.trim() : null;

        return problemRepository.searchProblems(
                user,
                searchParam,
                topicParam,
                difficulty,
                platformParam,
                status,
                revisionRequired,
                pageable).map(p -> {
                    long totalAttempts = attemptRepository.countByProblem(p);
                    long successAttempts = attemptRepository.countByProblemAndOutcome(p,
                            com.aischolartwin.entity.DSAAttemptOutcome.SUCCESS);
                    double successRate = totalAttempts > 0 ? ((double) successAttempts / totalAttempts) * 100.0 : 0.0;
                    return DSAProblemResponse.fromEntity(p, (int) totalAttempts, successRate);
                });
    }

    @Transactional(readOnly = true)
    public List<DSAProblemResponse> getProblemsByTopic(User user, String topic) {
        if (topic == null || topic.isBlank()) {
            throw new InvalidPayloadException("Topic parameter cannot be blank.");
        }
        return problemRepository.findByUserAndPrimaryTopic(user, topic.trim())
                .stream()
                .map(p -> {
                    long total = attemptRepository.countByProblem(p);
                    long succ = attemptRepository.countByProblemAndOutcome(p,
                            com.aischolartwin.entity.DSAAttemptOutcome.SUCCESS);
                    double rate = total > 0 ? ((double) succ / total) * 100.0 : 0.0;
                    return DSAProblemResponse.fromEntity(p, (int) total, rate);
                })
                .toList();
    }

    private void validateRequest(DSAProblemRequest request) {
        if (request == null) {
            throw new InvalidPayloadException("Problem request body cannot be null.");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Problem title is required.");
        }
        if (request.primaryTopic() == null || request.primaryTopic().isBlank()) {
            throw new InvalidPayloadException("Primary topic is required.");
        }
        if (request.difficulty() == null) {
            throw new InvalidPayloadException("Problem difficulty (EASY, MEDIUM, HARD) is required.");
        }
        if (request.problemUrl() != null && !request.problemUrl().isBlank()) {
            String url = request.problemUrl().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new InvalidPayloadException("Problem URL must begin with http:// or https://");
            }
        }
    }

    private String cleanUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return url.trim();
    }
}