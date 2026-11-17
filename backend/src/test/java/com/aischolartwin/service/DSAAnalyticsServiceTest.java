package com.aischolartwin.service;

import com.aischolartwin.dto.DSADashboardSummaryResponse;
import com.aischolartwin.dto.DSADifficultyAnalyticsResponse;
import com.aischolartwin.dto.DSATopicProgressResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DSAAnalyticsServiceTest {

    @Mock
    private DSAProblemRepository problemRepository;

    @Mock
    private DSAAttemptRepository attemptRepository;

    @Mock
    private DSARevisionRepository revisionRepository;

    private DSAAnalyticsService analyticsService;
    private User testUser;

    @BeforeEach
    void setUp() {
        analyticsService = new DSAAnalyticsService(problemRepository, attemptRepository, revisionRepository);
        testUser = new User("Coder One", "coder@univ.edu", "hashedSecret", Role.USER);
        testUser.setId(101L);
    }

    @Test
    @DisplayName("Calculate streaks when user practiced consecutively for 3 days")
    void getDashboardSummary_calculatesConsecutiveStreaks() {
        LocalDate today = LocalDate.now();
        List<LocalDate> consecutiveDates = List.of(
                today,
                today.minusDays(1),
                today.minusDays(2)
        );

        when(attemptRepository.findDistinctAttemptDatesByUser(testUser)).thenReturn(consecutiveDates);
        when(problemRepository.countByUser(testUser)).thenReturn(10L);
        when(problemRepository.countByUserAndStatus(testUser, DSAStatus.SOLVED)).thenReturn(7L);
        when(problemRepository.countByUserAndStatus(testUser, DSAStatus.ATTEMPTED)).thenReturn(2L);
        when(attemptRepository.countByUser(testUser)).thenReturn(12L);
        when(attemptRepository.countByUserAndOutcome(testUser, DSAAttemptOutcome.SUCCESS)).thenReturn(9L);
        when(revisionRepository.findByUserOrderByScheduledDateAsc(testUser)).thenReturn(Collections.emptyList());

        DSADashboardSummaryResponse summary = analyticsService.getDashboardSummary(testUser);

        assertEquals(10L, summary.totalProblems());
        assertEquals(7L, summary.solvedProblems());
        assertEquals(2L, summary.attemptedProblems());
        assertEquals(3L, summary.remainingProblems());
        assertEquals(3, summary.currentStreakDays());
        assertEquals(3, summary.longestStreakDays());
        assertEquals(75.0, summary.overallAccuracyPercent());
        assertFalse(summary.insights().isEmpty());
    }

    @Test
    @DisplayName("Calculate streak is 0 when latest attempt is older than yesterday")
    void getDashboardSummary_brokenStreakReturnsZero() {
        LocalDate today = LocalDate.now();
        List<LocalDate> oldDates = List.of(
                today.minusDays(3),
                today.minusDays(4)
        );

        when(attemptRepository.findDistinctAttemptDatesByUser(testUser)).thenReturn(oldDates);
        when(problemRepository.countByUser(testUser)).thenReturn(5L);
        when(revisionRepository.findByUserOrderByScheduledDateAsc(testUser)).thenReturn(Collections.emptyList());

        DSADashboardSummaryResponse summary = analyticsService.getDashboardSummary(testUser);

        assertEquals(0, summary.currentStreakDays());
        assertEquals(2, summary.longestStreakDays());
    }

    @Test
    @DisplayName("Difficulty analytics returns metrics for Easy, Medium, and Hard")
    void getDifficultyAnalytics_returnsAllMetrics() {
        when(problemRepository.countByUserAndDifficulty(testUser, DSADifficulty.EASY)).thenReturn(5L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.EASY, DSAStatus.SOLVED)).thenReturn(4L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.EASY, DSAStatus.ATTEMPTED)).thenReturn(1L);

        when(problemRepository.countByUserAndDifficulty(testUser, DSADifficulty.MEDIUM)).thenReturn(10L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.MEDIUM, DSAStatus.SOLVED)).thenReturn(5L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.MEDIUM, DSAStatus.ATTEMPTED)).thenReturn(3L);

        when(problemRepository.countByUserAndDifficulty(testUser, DSADifficulty.HARD)).thenReturn(2L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.HARD, DSAStatus.SOLVED)).thenReturn(0L);
        when(problemRepository.countByUserAndDifficultyAndStatus(testUser, DSADifficulty.HARD, DSAStatus.ATTEMPTED)).thenReturn(1L);

        DSADifficultyAnalyticsResponse analytics = analyticsService.getDifficultyAnalytics(testUser);

        assertNotNull(analytics.easy());
        assertEquals(5L, analytics.easy().total());
        assertEquals(4L, analytics.easy().solved());
        assertEquals(80.0, analytics.easy().completionRatePercent());

        assertNotNull(analytics.medium());
        assertEquals(10L, analytics.medium().total());
        assertEquals(5L, analytics.medium().solved());
        assertEquals(50.0, analytics.medium().completionRatePercent());

        assertNotNull(analytics.hard());
        assertEquals(2L, analytics.hard().total());
        assertEquals(0L, analytics.hard().solved());
        assertEquals(0.0, analytics.hard().completionRatePercent());
    }

    @Test
    @DisplayName("Topic progress lists all 20 standard topics")
    void getTopicProgressList_includesAllStandardTopics() {
        when(problemRepository.findByUserOrderByUpdatedAtDesc(testUser)).thenReturn(Collections.emptyList());
        when(attemptRepository.findByUserOrderByAttemptDateDescCreatedAtDesc(testUser)).thenReturn(Collections.emptyList());

        List<DSATopicProgressResponse> topics = analyticsService.getTopicProgressList(testUser);

        assertEquals(20, topics.size());
        assertTrue(topics.stream().anyMatch(t -> t.topicName().equals("Arrays")));
        assertTrue(topics.stream().anyMatch(t -> t.topicName().equals("Dynamic Programming")));
        assertTrue(topics.stream().anyMatch(t -> t.topicName().equals("Graphs")));
        assertTrue(topics.stream().anyMatch(t -> t.topicName().equals("Tries")));
    }
}

