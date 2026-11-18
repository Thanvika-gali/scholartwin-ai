package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.PlannerDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.PlannerTaskRepository;
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
class DailyStudyPlannerServiceTest {

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    @Mock
    private LearningProfileRepository profileRepository;

    @Mock
    private LearningTaskRepository learningTaskRepository;

    @Mock
    private SpacedRevisionItemRepository spacedRevisionItemRepository;

    private DailyStudyPlannerService plannerService;
    private User testUser;

    @BeforeEach
    void setUp() {
        plannerService = new DailyStudyPlannerService(
                plannerTaskRepository,
                profileRepository,
                learningTaskRepository,
                spacedRevisionItemRepository
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Dave Learner");
        testUser.setEmail("dave@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("getDailyPlan calculates planned vs completed minutes and completion percentage")
    void getDailyPlan_calculatesMetricsCorrectly() {
        LocalDate today = LocalDate.now();

        PlannerTask task1 = new PlannerTask(testUser, "Task 1", "Desc", PlannerTaskType.ROADMAP_TOPIC, today, PlannerTaskPriority.HIGH, 60, 1L, "TASK");
        task1.setStatus(PlannerTaskStatus.COMPLETED);
        task1.setActualMinutes(50);

        PlannerTask task2 = new PlannerTask(testUser, "Task 2", "Desc", PlannerTaskType.REVISION, today, PlannerTaskPriority.MEDIUM, 30, 2L, "REVISION");
        task2.setStatus(PlannerTaskStatus.PENDING);

        when(profileRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(plannerTaskRepository.findByUserAndScheduledDateOrderByPriorityDescCreatedAtAsc(testUser, today))
                .thenReturn(List.of(task1, task2));

        PlannerDtos.DailyPlanSummaryResponse plan = plannerService.getDailyPlan(testUser, today);

        assertNotNull(plan);
        assertEquals(90, plan.plannedMinutes());
        assertEquals(50, plan.completedMinutes());
        assertEquals(2, plan.totalTasksCount());
        assertEquals(1, plan.completedTasksCount());
        assertEquals(50, plan.completionPercentage());
    }

    @Test
    @DisplayName("rescheduleTask updates task date and notes")
    void rescheduleTask_updatesDateAndStatus() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        PlannerTask task = new PlannerTask(testUser, "Design DB Schema", "Desc", PlannerTaskType.CUSTOM, today, PlannerTaskPriority.HIGH, 45, null, null);
        task.setId(99L);

        when(plannerTaskRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.of(task));
        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(inv -> inv.getArgument(0));

        PlannerDtos.RescheduleTaskRequest request = new PlannerDtos.RescheduleTaskRequest(tomorrow, "Exam preparation took priority");

        PlannerDtos.PlannerTaskResponse response = plannerService.rescheduleTask(testUser, 99L, request);

        assertNotNull(response);
        assertEquals(tomorrow, response.scheduledDate());
        assertEquals(PlannerTaskStatus.RESCHEDULED, response.status());
        assertTrue(response.notes().contains("Exam preparation took priority"));
    }
}
