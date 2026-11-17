package com.aischolartwin.service;

import com.aischolartwin.dto.DSAAttemptRequest;
import com.aischolartwin.dto.DSAAttemptResponse;
import com.aischolartwin.dto.DSAAttemptStatsResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DSAAttemptServiceTest {

    @Mock
    private DSAAttemptRepository attemptRepository;

    @Mock
    private DSAProblemRepository problemRepository;

    @Mock
    private DSAProblemService problemService;

    private DSAAttemptService attemptService;
    private User testUser;
    private DSAProblem testProblem;

    @BeforeEach
    void setUp() {
        attemptService = new DSAAttemptService(attemptRepository, problemRepository, problemService);
        testUser = new User("Coder One", "coder@univ.edu", "hashedSecret", Role.USER);
        testUser.setId(101L);

        testProblem = new DSAProblem(testUser, "Binary Search", "https://leetcode.com/problems/binary-search/",
                "LeetCode", DSADifficulty.EASY, "Searching");
        testProblem.setId(201L);
        testProblem.setStatus(DSAStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("Log successful attempt automatically updates problem status to SOLVED")
    void logAttempt_success_promotesProblemToSolved() {
        when(problemService.getProblemEntity(testUser, 201L)).thenReturn(testProblem);
        when(attemptRepository.save(any(DSAAttempt.class))).thenAnswer(invocation -> {
            DSAAttempt a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        DSAAttemptRequest request = new DSAAttemptRequest(
                LocalDate.now(),
                DSAAttemptOutcome.SUCCESS,
                15,
                "Java",
                "Solved with standard while loop",
                "Off-by-one mid calculation"
        );

        DSAAttemptResponse response = attemptService.logAttempt(testUser, 201L, request);

        assertNotNull(response);
        assertEquals(DSAAttemptOutcome.SUCCESS, response.outcome());
        assertEquals(15, response.timeSpentMinutes());
        assertEquals(DSAStatus.SOLVED, testProblem.getStatus());
        assertNotNull(testProblem.getSolvedAt());
        verify(problemRepository, times(1)).save(testProblem);
    }

    @Test
    @DisplayName("Log failed attempt on NOT_STARTED problem updates status to ATTEMPTED")
    void logAttempt_failed_promotesProblemToAttempted() {
        when(problemService.getProblemEntity(testUser, 201L)).thenReturn(testProblem);
        when(attemptRepository.save(any(DSAAttempt.class))).thenAnswer(invocation -> {
            DSAAttempt a = invocation.getArgument(0);
            a.setId(2L);
            return a;
        });

        DSAAttemptRequest request = new DSAAttemptRequest(
                LocalDate.now(),
                DSAAttemptOutcome.FAILED,
                30,
                "Python",
                "Hit TLE on large test cases",
                "Time Limit Exceeded"
        );

        DSAAttemptResponse response = attemptService.logAttempt(testUser, 201L, request);

        assertNotNull(response);
        assertEquals(DSAAttemptOutcome.FAILED, response.outcome());
        assertEquals(DSAStatus.ATTEMPTED, testProblem.getStatus());
        assertNull(testProblem.getSolvedAt());
        verify(problemRepository, times(1)).save(testProblem);
    }

    @Test
    @DisplayName("Log attempt with negative time throws InvalidPayloadException")
    void logAttempt_negativeTime_throwsInvalidPayloadException() {
        DSAAttemptRequest request = new DSAAttemptRequest(
                LocalDate.now(),
                DSAAttemptOutcome.SUCCESS,
                -10,
                "C++",
                null, null
        );

        assertThrows(InvalidPayloadException.class, () -> attemptService.logAttempt(testUser, 201L, request));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get problem stats correctly computes success rate and average time")
    void getProblemStats_computesRateAndAverageTime() {
        when(problemService.getProblemEntity(testUser, 201L)).thenReturn(testProblem);
        when(attemptRepository.countByProblem(testProblem)).thenReturn(4L);
        when(attemptRepository.countByProblemAndOutcome(testProblem, DSAAttemptOutcome.SUCCESS)).thenReturn(3L);
        when(attemptRepository.findAverageTimeSpentByProblem(testProblem)).thenReturn(22.5);

        DSAAttemptStatsResponse stats = attemptService.getProblemStats(testUser, 201L);

        assertEquals(4L, stats.totalAttempts());
        assertEquals(3L, stats.successfulAttempts());
        assertEquals(1L, stats.failedAttempts());
        assertEquals(75.0, stats.successRatePercent());
        assertEquals(22.5, stats.averageTimeSpentMinutes());
    }
}

