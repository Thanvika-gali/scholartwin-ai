package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.SpacedRevisionDtos;
import com.aischolartwin.entity.DSARevision;
import com.aischolartwin.entity.DSARevisionStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DSARevisionRepository;
import com.aischolartwin.repository.learning.RevisionAttemptRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SpacedRevisionService {

    private final SpacedRevisionItemRepository itemRepository;
    private final RevisionAttemptRepository attemptRepository;
    private final DSARevisionRepository dsaRevisionRepository;

    public SpacedRevisionService(
            SpacedRevisionItemRepository itemRepository,
            RevisionAttemptRepository attemptRepository,
            DSARevisionRepository dsaRevisionRepository
    ) {
        this.itemRepository = itemRepository;
        this.attemptRepository = attemptRepository;
        this.dsaRevisionRepository = dsaRevisionRepository;
    }

    @Transactional(readOnly = true)
    public SpacedRevisionDtos.RevisionQueueSummaryResponse getRevisionQueue(User user) {
        LocalDate today = LocalDate.now();

        List<SpacedRevisionItem> allItems = itemRepository.findByUserOrderByScheduledDateAsc(user);
        List<SpacedRevisionItem> dueToday = new ArrayList<>();
        List<SpacedRevisionItem> overdue = new ArrayList<>();
        List<SpacedRevisionItem> upcoming = new ArrayList<>();
        List<SpacedRevisionItem> completed = new ArrayList<>();
        int needsAttentionCount = 0;

        for (SpacedRevisionItem item : allItems) {
            if (item.getStatus() == RevisionStatus.COMPLETED) {
                completed.add(item);
            } else if (item.getStatus() == RevisionStatus.NEEDS_RELEARNING) {
                needsAttentionCount++;
                dueToday.add(item);
            } else if (item.getScheduledDate().isBefore(today)) {
                item.setStatus(RevisionStatus.OVERDUE);
                overdue.add(item);
            } else if (item.getScheduledDate().isEqual(today)) {
                item.setStatus(RevisionStatus.DUE);
                dueToday.add(item);
            } else {
                item.setStatus(RevisionStatus.UPCOMING);
                upcoming.add(item);
            }
        }

        return new SpacedRevisionDtos.RevisionQueueSummaryResponse(
                dueToday.size(),
                overdue.size(),
                upcoming.size(),
                completed.size(),
                needsAttentionCount,
                dueToday.stream().map(SpacedRevisionDtos.SpacedRevisionResponse::fromEntity).toList(),
                overdue.stream().map(SpacedRevisionDtos.SpacedRevisionResponse::fromEntity).toList(),
                upcoming.stream().map(SpacedRevisionDtos.SpacedRevisionResponse::fromEntity).toList(),
                completed.stream().map(SpacedRevisionDtos.SpacedRevisionResponse::fromEntity).toList()
        );
    }

    @Transactional
    public SpacedRevisionDtos.SpacedRevisionResponse createRevisionItem(User user, SpacedRevisionDtos.CreateRevisionItemRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Revision topic title is required.");
        }

        LocalDate schedDate = request.scheduledDate() != null ? request.scheduledDate() : LocalDate.now().plusDays(1);
        String sequence = (request.customIntervalSequence() != null && !request.customIntervalSequence().isBlank())
                ? request.customIntervalSequence().trim()
                : "1,3,7,14,30";

        SpacedRevisionItem item = new SpacedRevisionItem(
                user,
                request.title().trim(),
                request.itemType() != null ? request.itemType() : RevisionItemType.CUSTOM_TOPIC,
                request.linkedReferenceId(),
                schedDate,
                sequence
        );
        item.setNotes(request.notes());

        SpacedRevisionItem saved = itemRepository.save(item);
        return SpacedRevisionDtos.SpacedRevisionResponse.fromEntity(saved);
    }

    @Transactional
    public SpacedRevisionDtos.SpacedRevisionResponse completeRevision(User user, Long itemId, SpacedRevisionDtos.CompleteRevisionRequest request) {
        SpacedRevisionItem item = itemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Spaced revision item not found with ID: " + itemId));

        if (request.rating() == null) {
            throw new InvalidPayloadException("Self-assessment rating is required (EASY, MODERATE, DIFFICULT, NEED_TO_RELEARN).");
        }

        RevisionRating rating = request.rating();
        item.setLastRating(rating);
        item.setLastReviewedAt(Instant.now());

        int prevStage = item.getStage();
        int prevInterval = item.getIntervalDays();
        List<Integer> intervals = parseIntervals(item.getCustomIntervalSequence());

        int newStage = prevStage;
        int newInterval = prevInterval;
        LocalDate nextDate;
        RevisionStatus newStatus;

        switch (rating) {
            case EASY -> {
                newStage = prevStage + 1;
                newInterval = getIntervalForStage(intervals, newStage);
                nextDate = LocalDate.now().plusDays(newInterval);
                item.setConsecutiveSuccessCount(item.getConsecutiveSuccessCount() + 1);
                newStatus = (newStage >= intervals.size()) ? RevisionStatus.COMPLETED : RevisionStatus.UPCOMING;
            }
            case MODERATE -> {
                newStage = prevStage + 1;
                newInterval = getIntervalForStage(intervals, newStage);
                nextDate = LocalDate.now().plusDays(newInterval);
                item.setConsecutiveSuccessCount(item.getConsecutiveSuccessCount() + 1);
                newStatus = (newStage >= intervals.size()) ? RevisionStatus.COMPLETED : RevisionStatus.UPCOMING;
            }
            case DIFFICULT -> {
                // Shorten interval, do not advance stage
                newInterval = Math.max(1, prevInterval / 2);
                nextDate = LocalDate.now().plusDays(Math.max(1, newInterval));
                newStatus = RevisionStatus.UPCOMING;
            }
            case NEED_TO_RELEARN -> {
                // Reset stage to 0, review again tomorrow
                newStage = 0;
                newInterval = 1;
                nextDate = LocalDate.now().plusDays(1);
                item.setConsecutiveSuccessCount(0);
                newStatus = RevisionStatus.NEEDS_RELEARNING;
            }
            default -> {
                nextDate = LocalDate.now().plusDays(3);
                newStatus = RevisionStatus.UPCOMING;
            }
        }

        item.setStage(newStage);
        item.setIntervalDays(newInterval);
        item.setScheduledDate(nextDate);
        item.setStatus(newStatus);

        RevisionAttempt attempt = new RevisionAttempt(
                item,
                user,
                rating,
                prevInterval,
                newInterval,
                prevStage,
                newStage,
                nextDate,
                request.reflectionNotes()
        );
        item.addAttempt(attempt);
        attemptRepository.save(attempt);

        // If this item was linked to a DSA revision or problem, update the corresponding record
        if (item.getItemType() == RevisionItemType.DSA_PROBLEM && item.getLinkedReferenceId() != null) {
            try {
                List<DSARevision> dsaRevs = dsaRevisionRepository.findByUserAndStatusOrderByScheduledDateAsc(user, DSARevisionStatus.PENDING);
                for (DSARevision dRev : dsaRevs) {
                    if (dRev.getProblem() != null && dRev.getProblem().getId().equals(item.getLinkedReferenceId())) {
                        if (rating == RevisionRating.EASY || rating == RevisionRating.MODERATE) {
                            dRev.setStatus(DSARevisionStatus.COMPLETED);
                            dRev.setCompletedAt(Instant.now());
                            dsaRevisionRepository.save(dRev);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        SpacedRevisionItem saved = itemRepository.save(item);
        return SpacedRevisionDtos.SpacedRevisionResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteRevisionItem(User user, Long itemId) {
        SpacedRevisionItem item = itemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Spaced revision item not found with ID: " + itemId));
        itemRepository.delete(item);
    }

    private List<Integer> parseIntervals(String seq) {
        if (seq == null || seq.isBlank()) return List.of(1, 3, 7, 14, 30);
        try {
            return Arrays.stream(seq.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .filter(i -> i > 0)
                    .toList();
        } catch (Exception e) {
            return List.of(1, 3, 7, 14, 30);
        }
    }

    private int getIntervalForStage(List<Integer> intervals, int stage) {
        if (stage < intervals.size()) {
            return intervals.get(stage);
        }
        // Beyond configured stages, multiply the last interval by 1.8
        int last = intervals.get(intervals.size() - 1);
        return (int) Math.round(last * Math.pow(1.8, stage - intervals.size() + 1));
    }
}
