package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.SpacedRevisionDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.DSARevisionRepository;
import com.aischolartwin.repository.learning.RevisionAttemptRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRevisionServiceTest {

    @Mock
    private SpacedRevisionItemRepository itemRepository;

    @Mock
    private RevisionAttemptRepository attemptRepository;

    @Mock
    private DSARevisionRepository dsaRevisionRepository;

    private SpacedRevisionService revisionService;
    private User testUser;

    @BeforeEach
    void setUp() {
        revisionService = new SpacedRevisionService(itemRepository, attemptRepository, dsaRevisionRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Charlie Scholar");
        testUser.setEmail("charlie@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("getRevisionQueue properly categorizes due today, overdue, and upcoming items")
    void getRevisionQueue_categorizesItemsProperly() {
        LocalDate today = LocalDate.now();

        SpacedRevisionItem overdueItem = new SpacedRevisionItem(testUser, "Binary Search Invariants", RevisionItemType.CUSTOM_TOPIC, 100L, today.minusDays(2), "1,3,7,14,30");
        overdueItem.setStatus(RevisionStatus.OVERDUE);

        SpacedRevisionItem dueItem = new SpacedRevisionItem(testUser, "LRU Cache Design", RevisionItemType.DSA_PROBLEM, 101L, today, "1,3,7,14,30");
        dueItem.setStatus(RevisionStatus.DUE);

        SpacedRevisionItem upcomingItem = new SpacedRevisionItem(testUser, "Database Indexing B-Trees", RevisionItemType.ROADMAP_TOPIC, 102L, today.plusDays(4), "1,3,7,14,30");
        upcomingItem.setStatus(RevisionStatus.UPCOMING);

        when(itemRepository.findByUserOrderByScheduledDateAsc(testUser))
                .thenReturn(List.of(overdueItem, dueItem, upcomingItem));

        SpacedRevisionDtos.RevisionQueueSummaryResponse response = revisionService.getRevisionQueue(testUser);

        assertNotNull(response);
        assertEquals(1, response.dueTodayCount());
        assertEquals(1, response.overdueCount());
        assertEquals(1, response.upcomingCount());
    }

    @Test
    @DisplayName("completeRevision with EASY rating advances interval")
    void completeRevision_easyRatingAdvancesInterval() {
        LocalDate today = LocalDate.now();
        SpacedRevisionItem item = new SpacedRevisionItem(testUser, "Java Memory Model", RevisionItemType.CUSTOM_TOPIC, 200L, today, "1,3,7,14,30");
        item.setId(55L);
        item.setIntervalDays(3);
        item.setStage(2);

        when(itemRepository.findByIdAndUser(55L, testUser)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(SpacedRevisionItem.class))).thenAnswer(inv -> inv.getArgument(0));

        SpacedRevisionDtos.CompleteRevisionRequest request = new SpacedRevisionDtos.CompleteRevisionRequest(
                RevisionRating.EASY,
                "Strong recall of happens-before relationship and volatile semantics"
        );

        SpacedRevisionDtos.SpacedRevisionResponse response = revisionService.completeRevision(testUser, 55L, request);

        assertNotNull(response);
        assertEquals(RevisionRating.EASY, response.lastRating());
        assertTrue(response.intervalDays() > 3, "EASY rating should increase the interval beyond 3 days");
        assertTrue(response.scheduledDate().isAfter(today.plusDays(3)));
        verify(attemptRepository).save(any(RevisionAttempt.class));
    }

    @Test
    @DisplayName("completeRevision with NEED_TO_RELEARN resets interval to 1 day")
    void completeRevision_needToRelearnResetsInterval() {
        LocalDate today = LocalDate.now();
        SpacedRevisionItem item = new SpacedRevisionItem(testUser, "Red-Black Tree Deletion", RevisionItemType.DSA_PROBLEM, 201L, today, "1,3,7,14,30");
        item.setId(56L);
        item.setIntervalDays(14);
        item.setStage(4);

        when(itemRepository.findByIdAndUser(56L, testUser)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(SpacedRevisionItem.class))).thenAnswer(inv -> inv.getArgument(0));

        SpacedRevisionDtos.CompleteRevisionRequest request = new SpacedRevisionDtos.CompleteRevisionRequest(
                RevisionRating.NEED_TO_RELEARN,
                "Could not recall color flipping and rotation cases"
        );

        SpacedRevisionDtos.SpacedRevisionResponse response = revisionService.completeRevision(testUser, 56L, request);

        assertNotNull(response);
        assertEquals(RevisionRating.NEED_TO_RELEARN, response.lastRating());
        assertEquals(RevisionStatus.NEEDS_RELEARNING, response.status());
        assertEquals(1, response.intervalDays(), "Interval should reset to 1 day on relearn");
        assertEquals(today.plusDays(1), response.scheduledDate());
    }
}
