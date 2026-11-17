package com.aischolartwin.service;

import com.aischolartwin.dto.DSARevisionQueueResponse;
import com.aischolartwin.dto.DSARevisionRequest;
import com.aischolartwin.dto.DSARevisionResponse;
import com.aischolartwin.dto.DSARevisionUpdateRequest;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
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
class DSARevisionServiceTest {

    @Mock
    private DSARevisionRepository revisionRepository;

    @Mock
    private DSAProblemRepository problemRepository;

    @Mock
    private DSAProblemService problemService;

    private DSARevisionService revisionService;
    private User testUser;
    private DSAProblem testProblem;

    @BeforeEach
    void setUp() {
        revisionService = new DSARevisionService(revisionRepository, problemRepository, problemService);
        testUser = new User("Coder One", "coder@univ.edu", "hashedSecret", Role.USER);
        testUser.setId(101L);

        testProblem = new DSAProblem(testUser, "LRU Cache", "https://leetcode.com/problems/lru-cache/",
                "LeetCode", DSADifficulty.MEDIUM, "Linked Lists");
        testProblem.setId(301L);
    }

    @Test
    @DisplayName("Schedule revision marks problem revisionRequired and saves revision")
    void scheduleRevision_succeedsAndMarksProblem() {
        when(problemService.getProblemEntity(testUser, 301L)).thenReturn(testProblem);
        when(revisionRepository.save(any(DSARevision.class))).thenAnswer(invocation -> {
            DSARevision r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        DSARevisionRequest request = new DSARevisionRequest(
                LocalDate.now().plusDays(3),
                "Review doubly linked list node removal logic"
        );

        DSARevisionResponse response = revisionService.scheduleRevision(testUser, 301L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertTrue(testProblem.isRevisionRequired());
        verify(problemRepository, times(1)).save(testProblem);
        verify(revisionRepository, times(1)).save(any(DSARevision.class));
    }

    @Test
    @DisplayName("Schedule revision with null date throws InvalidPayloadException")
    void scheduleRevision_nullDate_throwsInvalidPayloadException() {
        DSARevisionRequest request = new DSARevisionRequest(null, "Notes");
        assertThrows(InvalidPayloadException.class, () -> revisionService.scheduleRevision(testUser, 301L, request));
    }

    @Test
    @DisplayName("Completing revision sets completedAt and clears revisionRequired if none pending")
    void updateRevision_markCompleted_updatesStatus() {
        testProblem.setRevisionRequired(true);
        DSARevision revision = new DSARevision(testProblem, testUser, LocalDate.now(), "Notes");
        revision.setId(10L);

        when(revisionRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(revision));
        when(revisionRepository.existsByProblemAndStatus(testProblem, DSARevisionStatus.PENDING)).thenReturn(false);
        when(revisionRepository.save(any(DSARevision.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DSARevisionUpdateRequest updateReq = new DSARevisionUpdateRequest(
                null, "Mastered eviction edge case", DSARevisionStatus.COMPLETED
        );

        DSARevisionResponse updated = revisionService.updateRevision(testUser, 10L, updateReq);

        assertEquals(DSARevisionStatus.COMPLETED, updated.status());
        assertNotNull(revision.getCompletedAt());
        assertFalse(testProblem.isRevisionRequired());
        verify(problemRepository, times(1)).save(testProblem);
    }

    @Test
    @DisplayName("Revision queue correctly categorizes overdue, due today, upcoming, and completed")
    void getRevisionQueue_categorizesProperly() {
        LocalDate today = LocalDate.now();

        DSARevision overdue = new DSARevision(testProblem, testUser, today.minusDays(2), "Overdue rev");
        overdue.setId(1L);

        DSARevision dueToday = new DSARevision(testProblem, testUser, today, "Due today rev");
        dueToday.setId(2L);

        DSARevision upcoming = new DSARevision(testProblem, testUser, today.plusDays(4), "Upcoming rev");
        upcoming.setId(3L);

        DSARevision completed = new DSARevision(testProblem, testUser, today.minusDays(5), "Completed rev");
        completed.setId(4L);
        completed.setStatus(DSARevisionStatus.COMPLETED);

        when(revisionRepository.findByUserOrderByScheduledDateAsc(testUser))
                .thenReturn(List.of(overdue, dueToday, upcoming, completed));

        DSARevisionQueueResponse queue = revisionService.getRevisionQueue(testUser);

        assertEquals(1, queue.overdue().size());
        assertEquals(1, queue.dueToday().size());
        assertEquals(1, queue.upcoming().size());
        assertEquals(1, queue.completed().size());
        assertEquals(3L, queue.totalPendingCount());
    }
}

