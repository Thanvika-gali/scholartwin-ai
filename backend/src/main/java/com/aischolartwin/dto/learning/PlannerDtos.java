package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.PlannerTask;
import com.aischolartwin.entity.learning.PlannerTaskPriority;
import com.aischolartwin.entity.learning.PlannerTaskStatus;
import com.aischolartwin.entity.learning.PlannerTaskType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class PlannerDtos {

    public record CreatePlannerTaskRequest(
            String title,
            String description,
            PlannerTaskType taskType,
            LocalDate scheduledDate,
            PlannerTaskPriority priority,
            Integer estimatedMinutes,
            Long linkedReferenceId,
            String linkedReferenceType,
            String notes
    ) {}

    public record UpdatePlannerTaskRequest(
            String title,
            String description,
            PlannerTaskType taskType,
            LocalDate scheduledDate,
            PlannerTaskPriority priority,
            Integer estimatedMinutes,
            Integer actualMinutes,
            PlannerTaskStatus status,
            String notes
    ) {}

    public record RescheduleTaskRequest(
            LocalDate newDate,
            String reason
    ) {}

    public record PlannerTaskResponse(
            Long id,
            String title,
            String description,
            PlannerTaskType taskType,
            LocalDate scheduledDate,
            PlannerTaskPriority priority,
            Integer estimatedMinutes,
            Integer actualMinutes,
            PlannerTaskStatus status,
            Instant completedAt,
            Long linkedReferenceId,
            String linkedReferenceType,
            String notes
    ) {
        public static PlannerTaskResponse fromEntity(PlannerTask task) {
            return new PlannerTaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getTaskType(),
                    task.getScheduledDate(),
                    task.getPriority(),
                    task.getEstimatedMinutes(),
                    task.getActualMinutes(),
                    task.getStatus(),
                    task.getCompletedAt(),
                    task.getLinkedReferenceId(),
                    task.getLinkedReferenceType(),
                    task.getNotes()
            );
        }
    }

    public record DailyPlanSummaryResponse(
            LocalDate date,
            int dailyAvailableMinutes,
            int plannedMinutes,
            int completedMinutes,
            int totalTasksCount,
            int completedTasksCount,
            int completionPercentage,
            List<PlannerTaskResponse> tasks
    ) {}
}
