package com.aischolartwin.service;

import com.aischolartwin.dto.DSARevisionQueueResponse;
import com.aischolartwin.dto.DSARevisionRequest;
import com.aischolartwin.dto.DSARevisionResponse;
import com.aischolartwin.dto.DSARevisionUpdateRequest;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSARevision;
import com.aischolartwin.entity.DSARevisionStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.entity.learning.RevisionItemType;
import com.aischolartwin.entity.learning.RevisionStatus;
import com.aischolartwin.entity.learning.SpacedRevisionItem;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DSARevisionService {

    private final DSARevisionRepository revisionRepository;
    private final DSAProblemRepository problemRepository;
    private final DSAProblemService problemService;
    private final SpacedRevisionItemRepository spacedRevisionItemRepository;

    public DSARevisionService(
            DSARevisionRepository revisionRepository,
            DSAProblemRepository problemRepository,
            DSAProblemService problemService
    ) {
        this(revisionRepository, problemRepository, problemService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public DSARevisionService(
            DSARevisionRepository revisionRepository,
            DSAProblemRepository problemRepository,
            DSAProblemService problemService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) SpacedRevisionItemRepository spacedRevisionItemRepository
    ) {
        this.revisionRepository = revisionRepository;
        this.problemRepository = problemRepository;
        this.problemService = problemService;
        this.spacedRevisionItemRepository = spacedRevisionItemRepository;
    }

    @Transactional
    public DSARevisionResponse scheduleRevision(User user, Long problemId, DSARevisionRequest request) {
        if (request == null || request.scheduledDate() == null) {
            throw new InvalidPayloadException("Scheduled revision date is required.");
        }

        DSAProblem problem = problemService.getProblemEntity(user, problemId);

        DSARevision revision = new DSARevision(
                problem,
                user,
                request.scheduledDate(),
                request.revisionNotes()
        );

        problem.setRevisionRequired(true);
        problemRepository.save(problem);

        DSARevision saved = revisionRepository.save(revision);
        return DSARevisionResponse.fromEntity(saved);
    }

    @Transactional
    public DSARevisionResponse updateRevision(User user, Long revisionId, DSARevisionUpdateRequest request) {
        DSARevision revision = revisionRepository.findByIdAndUser(revisionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("DSA Revision not found with ID: " + revisionId));

        if (request != null) {
            if (request.scheduledDate() != null) {
                revision.setScheduledDate(request.scheduledDate());
            }
            if (request.revisionNotes() != null) {
                revision.setRevisionNotes(request.revisionNotes());
            }
            if (request.status() != null) {
                revision.setStatus(request.status());
                if (request.status() == DSARevisionStatus.COMPLETED) {
                    revision.setCompletedAt(Instant.now());

                    // Check if other pending revisions exist for the problem
                    boolean hasPending = revisionRepository.existsByProblemAndStatus(
                            revision.getProblem(), DSARevisionStatus.PENDING);
                    if (!hasPending) {
                        DSAProblem problem = revision.getProblem();
                        problem.setRevisionRequired(false);
                        problemRepository.save(problem);
                    }

                    // Sync corresponding SpacedRevisionItem in Milestone 8 if present
                    if (spacedRevisionItemRepository != null) {
                        try {
                            List<SpacedRevisionItem> items = spacedRevisionItemRepository.findByUserOrderByScheduledDateAsc(user);
                            for (SpacedRevisionItem item : items) {
                                if (item.getItemType() == RevisionItemType.DSA_PROBLEM
                                        && item.getLinkedReferenceId() != null
                                        && item.getLinkedReferenceId().equals(revision.getProblem().getId())) {
                                    item.setStatus(RevisionStatus.COMPLETED);
                                    item.setLastReviewedAt(Instant.now());
                                    spacedRevisionItemRepository.save(item);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                } else {
                    revision.setCompletedAt(null);
                }
            }
        }

        DSARevision updated = revisionRepository.save(revision);
        return DSARevisionResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteRevision(User user, Long revisionId) {
        DSARevision revision = revisionRepository.findByIdAndUser(revisionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("DSA Revision not found with ID: " + revisionId));

        DSAProblem problem = revision.getProblem();
        revisionRepository.delete(revision);

        boolean hasPending = revisionRepository.existsByProblemAndStatus(problem, DSARevisionStatus.PENDING);
        if (!hasPending) {
            problem.setRevisionRequired(false);
            problemRepository.save(problem);
        }
    }

    @Transactional(readOnly = true)
    public List<DSARevisionResponse> getRevisionsForProblem(User user, Long problemId) {
        DSAProblem problem = problemService.getProblemEntity(user, problemId);
        return revisionRepository.findByProblemOrderByScheduledDateDesc(problem)
                .stream()
                .map(DSARevisionResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DSARevisionQueueResponse getRevisionQueue(User user) {
        LocalDate today = LocalDate.now();
        List<DSARevision> allUserRevisions = revisionRepository.findByUserOrderByScheduledDateAsc(user);

        List<DSARevisionResponse> overdue = new ArrayList<>();
        List<DSARevisionResponse> dueToday = new ArrayList<>();
        List<DSARevisionResponse> upcoming = new ArrayList<>();
        List<DSARevisionResponse> completed = new ArrayList<>();

        for (DSARevision r : allUserRevisions) {
            DSARevisionResponse resp = DSARevisionResponse.fromEntity(r);
            if (r.getStatus() == DSARevisionStatus.COMPLETED) {
                completed.add(resp);
            } else if (r.getScheduledDate().isBefore(today)) {
                overdue.add(resp);
            } else if (r.getScheduledDate().isEqual(today)) {
                dueToday.add(resp);
            } else {
                upcoming.add(resp);
            }
        }

        long totalPending = overdue.size() + dueToday.size() + upcoming.size();
        return new DSARevisionQueueResponse(overdue, dueToday, upcoming, completed, totalPending);
    }
}

