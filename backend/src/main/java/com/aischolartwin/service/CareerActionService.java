package com.aischolartwin.service;

import com.aischolartwin.dto.CareerActionRequest;
import com.aischolartwin.dto.CareerActionResponse;
import com.aischolartwin.dto.CareerActionUpdateRequest;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.CareerActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class CareerActionService {

    private final CareerActionRepository careerActionRepository;

    public CareerActionService(CareerActionRepository careerActionRepository) {
        this.careerActionRepository = careerActionRepository;
    }

    @Transactional(readOnly = true)
    public List<CareerActionResponse> getActions(User user, CareerActionStatus status) {
        List<CareerAction> list = (status != null)
                ? careerActionRepository.findByUserAndStatusOrderBySuggestedDeadlineAscCreatedAtDesc(user, status)
                : careerActionRepository.findByUserOrderBySuggestedDeadlineAscCreatedAtDesc(user);

        return list.stream().map(CareerActionResponse::fromEntity).toList();
    }

    @Transactional
    public CareerActionResponse createAction(User user, CareerActionRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Action title is required.");
        }

        String description = (request.description() != null && !request.description().isBlank())
                ? request.description().trim()
                : request.title().trim();

        CareerAction action = new CareerAction(
                user,
                request.title().trim(),
                request.description().trim(),
                request.reasonAndEvidence(),
                request.priority() != null ? request.priority() : CareerActionPriority.MEDIUM,
                request.category() != null ? request.category() : CareerActionCategory.GENERAL,
                request.relatedSkillOrRole(),
                request.suggestedDeadline() != null ? request.suggestedDeadline() : LocalDate.now().plusDays(7)
        );

        CareerAction saved = careerActionRepository.save(action);
        return CareerActionResponse.fromEntity(saved);
    }

    @Transactional
    public CareerActionResponse updateAction(User user, Long id, CareerActionUpdateRequest request) {
        CareerAction action = careerActionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Career action not found with ID: " + id));

        if (request.title() != null && !request.title().isBlank()) {
            action.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            action.setDescription(request.description().trim());
        }
        if (request.reasonAndEvidence() != null) {
            action.setReasonAndEvidence(request.reasonAndEvidence());
        }
        if (request.priority() != null) {
            action.setPriority(request.priority());
        }
        if (request.category() != null) {
            action.setCategory(request.category());
        }
        if (request.relatedSkillOrRole() != null) {
            action.setRelatedSkillOrRole(request.relatedSkillOrRole());
        }
        if (request.suggestedDeadline() != null) {
            action.setSuggestedDeadline(request.suggestedDeadline());
        }

        if (request.status() != null) {
            if (action.getStatus() != CareerActionStatus.COMPLETED && request.status() == CareerActionStatus.COMPLETED) {
                action.setCompletedAt(Instant.now());
            } else if (request.status() != CareerActionStatus.COMPLETED) {
                action.setCompletedAt(null);
            }
            action.setStatus(request.status());
        }

        CareerAction updated = careerActionRepository.save(action);
        return CareerActionResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteAction(User user, Long id) {
        CareerAction action = careerActionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Career action not found with ID: " + id));
        careerActionRepository.delete(action);
    }

    @Transactional
    public void generateRecommendedActionsFromAnalysis(User user, ResumeAnalysis analysis) {
        if (analysis == null) return;

        // Measurable results < 10
        if (analysis.getMeasurableResultsScore() < 10) {
            createIfAbsent(user,
                    "Quantify Engineering Impact in Projects",
                    "Update at least 2 project bullet points with measurable metrics (e.g. latency reduced by X%, handled Y requests, user count).",
                    "Resume Analysis showed a score of " + analysis.getMeasurableResultsScore() + "/15 in measurable engineering outcomes.",
                    CareerActionPriority.HIGH,
                    CareerActionCategory.RESUME_IMPROVEMENT,
                    "Resume",
                    LocalDate.now().plusDays(3)
            );
        }

        // Projects depth < 15
        if (analysis.getProjectsDepthScore() < 15) {
            createIfAbsent(user,
                    "Add or Expand a High-Impact Technical Project",
                    "Build or document a robust full-stack or distributed systems project with comprehensive README and live deployment.",
                    "Resume Analysis identified project descriptions and technical depth as an area for improvement.",
                    CareerActionPriority.HIGH,
                    CareerActionCategory.PROJECT_CREATION,
                    "Projects",
                    LocalDate.now().plusDays(14)
            );
        }

        // Structure < 8
        if (analysis.getStructureReadabilityScore() < 8) {
            createIfAbsent(user,
                    "Complete Missing Contact Links in Resume Header",
                    "Ensure active GitHub and LinkedIn profile URLs are included in the top contact section.",
                    "Missing online profile links were detected during automated parsing.",
                    CareerActionPriority.MEDIUM,
                    CareerActionCategory.RESUME_IMPROVEMENT,
                    "Contact Details",
                    LocalDate.now().plusDays(2)
            );
        }
    }

    private void createIfAbsent(User user, String title, String description, String evidence,
                                CareerActionPriority priority, CareerActionCategory category,
                                String related, LocalDate deadline) {
        List<CareerAction> existing = careerActionRepository.findByUserAndStatusOrderBySuggestedDeadlineAscCreatedAtDesc(
                user, CareerActionStatus.NOT_STARTED);
        boolean alreadyExists = existing.stream().anyMatch(a -> a.getTitle().equalsIgnoreCase(title));

        if (!alreadyExists) {
            CareerAction action = new CareerAction(user, title, description, evidence, priority, category, related, deadline);
            careerActionRepository.save(action);
        }
    }
}

