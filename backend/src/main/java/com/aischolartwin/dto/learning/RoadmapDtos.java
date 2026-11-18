package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class RoadmapDtos {

    public record CreateRoadmapRequest(
            String title,
            String description,
            String targetRole,
            String goalType,
            String targetSkillOrTopic,
            LocalDate targetCompletionDate,
            LearningPace pace
    ) {}

    public record UpdateRoadmapRequest(
            String title,
            String description,
            RoadmapStatus status,
            LocalDate targetCompletionDate
    ) {}

    public record CreateTaskRequest(
            String title,
            String topic,
            String subtopics,
            Integer estimatedMinutes,
            String learningObjectives,
            String practicalExercises,
            String miniProject,
            String completionCriteria,
            Long prerequisiteTaskId,
            String prerequisiteTitle
    ) {}

    public record UpdateTaskStatusRequest(
            TaskStatus status,
            Integer actualMinutes
    ) {}

    public record LearningTaskResponse(
            Long id,
            Long phaseId,
            String title,
            String topic,
            String subtopics,
            Integer taskOrder,
            Integer estimatedMinutes,
            Integer actualMinutes,
            String learningObjectives,
            String practicalExercises,
            String miniProject,
            String revisionCheckpoints,
            String completionCriteria,
            Long prerequisiteTaskId,
            String prerequisiteTitle,
            TaskStatus status,
            Instant completedAt
    ) {
        public static LearningTaskResponse fromEntity(LearningTask task) {
            return new LearningTaskResponse(
                    task.getId(),
                    task.getPhase() != null ? task.getPhase().getId() : null,
                    task.getTitle(),
                    task.getTopic(),
                    task.getSubtopics(),
                    task.getTaskOrder(),
                    task.getEstimatedMinutes(),
                    task.getActualMinutes(),
                    task.getLearningObjectives(),
                    task.getPracticalExercises(),
                    task.getMiniProject(),
                    task.getRevisionCheckpoints(),
                    task.getCompletionCriteria(),
                    task.getPrerequisiteTaskId(),
                    task.getPrerequisiteTitle(),
                    task.getStatus(),
                    task.getCompletedAt()
            );
        }
    }

    public record RoadmapPhaseResponse(
            Long id,
            Integer phaseNumber,
            String title,
            String description,
            Integer phaseOrder,
            List<LearningTaskResponse> tasks
    ) {
        public static RoadmapPhaseResponse fromEntity(RoadmapPhase phase) {
            List<LearningTaskResponse> taskResponses = phase.getTasks() != null
                    ? phase.getTasks().stream().map(LearningTaskResponse::fromEntity).toList()
                    : List.of();
            return new RoadmapPhaseResponse(
                    phase.getId(),
                    phase.getPhaseNumber(),
                    phase.getTitle(),
                    phase.getDescription(),
                    phase.getPhaseOrder(),
                    taskResponses
            );
        }
    }

    public record LearningRoadmapResponse(
            Long id,
            String title,
            String description,
            String targetRole,
            String goalType,
            String targetSkillOrTopic,
            RoadmapStatus status,
            LocalDate targetCompletionDate,
            Double estimatedHours,
            Double completedHours,
            Integer completionPercentage,
            List<RoadmapPhaseResponse> phases,
            Instant createdAt
    ) {
        public static LearningRoadmapResponse fromEntity(LearningRoadmap roadmap) {
            List<RoadmapPhaseResponse> phaseResponses = roadmap.getPhases() != null
                    ? roadmap.getPhases().stream().map(RoadmapPhaseResponse::fromEntity).toList()
                    : List.of();
            return new LearningRoadmapResponse(
                    roadmap.getId(),
                    roadmap.getTitle(),
                    roadmap.getDescription(),
                    roadmap.getTargetRole(),
                    roadmap.getGoalType(),
                    roadmap.getTargetSkillOrTopic(),
                    roadmap.getStatus(),
                    roadmap.getTargetCompletionDate(),
                    roadmap.getEstimatedHours(),
                    roadmap.getCompletedHours(),
                    roadmap.getCompletionPercentage(),
                    phaseResponses,
                    roadmap.getCreatedAt()
            );
        }
    }
}
