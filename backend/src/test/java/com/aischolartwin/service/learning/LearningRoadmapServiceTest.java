package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.RoadmapDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.learning.LearningRoadmapRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.RoadmapPhaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningRoadmapServiceTest {

    @Mock
    private LearningRoadmapRepository roadmapRepository;

    @Mock
    private RoadmapPhaseRepository phaseRepository;

    @Mock
    private LearningTaskRepository taskRepository;

    private LearningRoadmapService roadmapService;
    private User testUser;

    @BeforeEach
    void setUp() {
        roadmapService = new LearningRoadmapService(roadmapRepository, phaseRepository, taskRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Bob Dev");
        testUser.setEmail("bob@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("createOrGenerateRoadmap validates title presence")
    void createOrGenerateRoadmap_throwsOnEmptyTitle() {
        RoadmapDtos.CreateRoadmapRequest request = new RoadmapDtos.CreateRoadmapRequest(
                "",
                "Description",
                "Software Development Engineer",
                "ROLE",
                "Backend",
                LocalDate.now().plusMonths(3),
                LearningPace.MODERATE
        );

        assertThrows(InvalidPayloadException.class, () -> roadmapService.createOrGenerateRoadmap(testUser, request));
    }

    @Test
    @DisplayName("createOrGenerateRoadmap populates structured SDE phases and tasks")
    void createOrGenerateRoadmap_generatesSdePhases() {
        when(roadmapRepository.save(any(LearningRoadmap.class))).thenAnswer(inv -> {
            LearningRoadmap r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        when(phaseRepository.save(any(RoadmapPhase.class))).thenAnswer(inv -> {
            RoadmapPhase p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });

        when(taskRepository.save(any(LearningTask.class))).thenAnswer(inv -> {
            LearningTask t = inv.getArgument(0);
            t.setId(300L);
            return t;
        });

        when(roadmapRepository.findDetailedByIdAndUser(eq(100L), eq(testUser))).thenAnswer(inv -> {
            LearningRoadmap r = new LearningRoadmap(testUser, "SDE Mastery", "Desc", "Software Development Engineer", "ROLE", "Java");
            r.setId(100L);
            return Optional.of(r);
        });

        RoadmapDtos.CreateRoadmapRequest request = new RoadmapDtos.CreateRoadmapRequest(
                "SDE Mastery",
                "Comprehensive roadmap to crack tier-1 SDE roles",
                "Software Development Engineer",
                "ROLE",
                "Backend",
                LocalDate.now().plusMonths(4),
                LearningPace.MODERATE
        );

        RoadmapDtos.LearningRoadmapResponse response = roadmapService.createOrGenerateRoadmap(testUser, request);

        assertNotNull(response);
        assertEquals("SDE Mastery", response.title());
        verify(phaseRepository, atLeast(2)).save(any(RoadmapPhase.class));
        verify(taskRepository, atLeast(3)).save(any(LearningTask.class));
    }

    @Test
    @DisplayName("updateTaskStatus correctly marks task completed and recalculates progress")
    void updateTaskStatus_marksCompleted() {
        LearningRoadmap roadmap = new LearningRoadmap(testUser, "SDE Roadmap", "Desc", "SDE", "ROLE", "Java");
        roadmap.setId(50L);

        RoadmapPhase phase = new RoadmapPhase(roadmap, 1, "Phase 1: Foundations", "Foundations", 1);
        phase.setId(60L);

        LearningTask task = new LearningTask(phase, testUser, "Arrays & Pointers", "DSA", "Two Pointers", 1, 60, "Master array ops", "Implement sliding window", "Pass all tests");
        task.setId(70L);

        when(taskRepository.findByIdAndUser(70L, testUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(LearningTask.class))).thenAnswer(inv -> inv.getArgument(0));

        RoadmapDtos.UpdateTaskStatusRequest request = new RoadmapDtos.UpdateTaskStatusRequest(TaskStatus.COMPLETED, 55);

        RoadmapDtos.LearningTaskResponse response = roadmapService.updateTaskStatus(testUser, 70L, request);

        assertNotNull(response);
        assertEquals(TaskStatus.COMPLETED, response.status());
        assertEquals(55, response.actualMinutes());
        assertNotNull(task.getCompletedAt());
    }
}
