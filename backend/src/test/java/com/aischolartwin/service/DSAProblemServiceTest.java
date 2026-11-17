package com.aischolartwin.service;

import com.aischolartwin.dto.DSAProblemRequest;
import com.aischolartwin.dto.DSAProblemResponse;
import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DSAProblemServiceTest {

    @Mock
    private DSAProblemRepository problemRepository;

    @Mock
    private DSAAttemptRepository attemptRepository;

    @Mock
    private DSARevisionRepository revisionRepository;

    private DSAProblemService problemService;
    private User testUser;

    @BeforeEach
    void setUp() {
        problemService = new DSAProblemService(problemRepository, attemptRepository, revisionRepository);
        testUser = new User("Coder One", "coder@univ.edu", "hashedSecret", Role.USER);
        testUser.setId(101L);
    }

    @Test
    @DisplayName("Create problem with valid data succeeds")
    void createProblem_validData_succeeds() {
        DSAProblemRequest request = new DSAProblemRequest(
                "Two Sum",
                "https://leetcode.com/problems/two-sum/",
                "LeetCode",
                DSADifficulty.EASY,
                "Arrays",
                Set.of("Hashing"),
                DSAStatus.NOT_STARTED,
                "Use a hash map for O(n) lookup",
                "Single pass hash table",
                "O(N)",
                "O(N)",
                false
        );

        when(problemRepository.save(any(DSAProblem.class))).thenAnswer(invocation -> {
            DSAProblem p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        DSAProblemResponse response = problemService.createProblem(testUser, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Two Sum", response.title());
        assertEquals("https://leetcode.com/problems/two-sum/", response.problemUrl());
        assertEquals(DSADifficulty.EASY, response.difficulty());
        assertEquals("Arrays", response.primaryTopic());
        assertTrue(response.additionalTopics().contains("Hashing"));
        assertEquals(DSAStatus.NOT_STARTED, response.status());
        verify(problemRepository, times(1)).save(any(DSAProblem.class));
    }

    @Test
    @DisplayName("Create problem with invalid URL format throws InvalidPayloadException")
    void createProblem_invalidUrl_throwsInvalidPayloadException() {
        DSAProblemRequest request = new DSAProblemRequest(
                "Invalid URL Problem",
                "ftp://invalid-url.com/problem",
                "LeetCode",
                DSADifficulty.EASY,
                "Arrays",
                null,
                DSAStatus.NOT_STARTED,
                null, null, null, null, false
        );

        assertThrows(InvalidPayloadException.class, () -> problemService.createProblem(testUser, request));
        verify(problemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create problem with blank title throws InvalidPayloadException")
    void createProblem_blankTitle_throwsInvalidPayloadException() {
        DSAProblemRequest request = new DSAProblemRequest(
                "   ",
                null,
                "LeetCode",
                DSADifficulty.MEDIUM,
                "Strings",
                null,
                DSAStatus.NOT_STARTED,
                null, null, null, null, false
        );

        assertThrows(InvalidPayloadException.class, () -> problemService.createProblem(testUser, request));
        verify(problemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get problem for unauthorized user throws ResourceNotFoundException")
    void getProblem_unauthorizedUser_throwsResourceNotFoundException() {
        when(problemRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> problemService.getProblem(testUser, 999L));
    }

    @Test
    @DisplayName("Update problem updates fields and sets solvedAt when marking SOLVED")
    void updateProblem_markingSolved_setsSolvedAt() {
        DSAProblem existing = new DSAProblem(testUser, "Old Title", null, "LeetCode", DSADifficulty.MEDIUM, "Arrays");
        existing.setId(5L);
        existing.setStatus(DSAStatus.NOT_STARTED);

        when(problemRepository.findByIdAndUser(5L, testUser)).thenReturn(Optional.of(existing));
        when(problemRepository.save(any(DSAProblem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DSAProblemRequest updateReq = new DSAProblemRequest(
                "New Title",
                "https://leetcode.com/problems/new-title/",
                "LeetCode",
                DSADifficulty.HARD,
                "Dynamic Programming",
                Set.of("Memoization"),
                DSAStatus.SOLVED,
                "Notes updated",
                "Bottom up DP",
                "O(N^2)",
                "O(N)",
                true
        );

        DSAProblemResponse updated = problemService.updateProblem(testUser, 5L, updateReq);

        assertEquals("New Title", updated.title());
        assertEquals(DSADifficulty.HARD, updated.difficulty());
        assertEquals("Dynamic Programming", updated.primaryTopic());
        assertEquals(DSAStatus.SOLVED, updated.status());
        assertNotNull(updated.solvedAt());
        assertTrue(updated.revisionRequired());
    }
}

