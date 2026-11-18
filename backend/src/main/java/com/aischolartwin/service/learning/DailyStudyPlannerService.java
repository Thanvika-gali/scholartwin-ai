package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.PlannerDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.PlannerTaskRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class DailyStudyPlannerService {

    private final PlannerTaskRepository plannerTaskRepository;
    private final LearningProfileRepository profileRepository;
    private final LearningTaskRepository learningTaskRepository;
    private final SpacedRevisionItemRepository spacedRevisionItemRepository;

    public DailyStudyPlannerService(
            PlannerTaskRepository plannerTaskRepository,
            LearningProfileRepository profileRepository,
            LearningTaskRepository learningTaskRepository,
            SpacedRevisionItemRepository spacedRevisionItemRepository
    ) {
        this.plannerTaskRepository = plannerTaskRepository;
        this.profileRepository = profileRepository;
        this.learningTaskRepository = learningTaskRepository;
        this.spacedRevisionItemRepository = spacedRevisionItemRepository;
    }

    @Transactional(readOnly = true)
    public PlannerDtos.DailyPlanSummaryResponse getDailyPlan(User user, LocalDate targetDate) {
        LocalDate date = targetDate != null ? targetDate : LocalDate.now();

        LearningProfile profile = profileRepository.findByUser(user).orElse(null);
        int dailyAvailable = profile != null && profile.getDailyStudyMinutes() != null ? profile.getDailyStudyMinutes() : 60;

        List<PlannerTask> tasks = plannerTaskRepository.findByUserAndScheduledDateOrderByPriorityDescCreatedAtAsc(user, date);

        int plannedMinutes = 0;
        int completedMinutes = 0;
        int completedCount = 0;

        for (PlannerTask task : tasks) {
            plannedMinutes += task.getEstimatedMinutes();
            if (task.getStatus() == PlannerTaskStatus.COMPLETED) {
                completedCount++;
                completedMinutes += (task.getActualMinutes() > 0 ? task.getActualMinutes() : task.getEstimatedMinutes());
            }
        }

        int completionPct = !tasks.isEmpty() ? (int) Math.round(((double) completedCount / tasks.size()) * 100) : 0;
        List<PlannerDtos.PlannerTaskResponse> taskResponses = tasks.stream().map(PlannerDtos.PlannerTaskResponse::fromEntity).toList();

        return new PlannerDtos.DailyPlanSummaryResponse(
                date,
                dailyAvailable,
                plannedMinutes,
                completedMinutes,
                tasks.size(),
                completedCount,
                completionPct,
                taskResponses
        );
    }

    @Transactional
    public PlannerDtos.PlannerTaskResponse createPlannerTask(User user, PlannerDtos.CreatePlannerTaskRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidPayloadException("Planner task title is required.");
        }

        LocalDate date = request.scheduledDate() != null ? request.scheduledDate() : LocalDate.now();

        // 1. Conflict Prevention: Prevent duplicate task scheduling for the same topic on the same day
        List<PlannerTask> existingTasks = plannerTaskRepository.findByUserAndScheduledDateOrderByPriorityDescCreatedAtAsc(user, date);
        String cleanTitle = request.title().trim();
        boolean isDuplicate = existingTasks.stream().anyMatch(t ->
                t.getTitle().equalsIgnoreCase(cleanTitle) ||
                (request.linkedReferenceId() != null && request.linkedReferenceId().equals(t.getLinkedReferenceId()) &&
                 Objects.equals(request.linkedReferenceType(), t.getLinkedReferenceType()))
        );
        if (isDuplicate) {
            throw new InvalidPayloadException("A planner task for '" + cleanTitle + "' is already scheduled on " + date + ".");
        }

        // 2. Deadline Check: If linked to a learning task, ensure not scheduled past target deadline
        if (request.linkedReferenceId() != null && "LEARNING_TASK".equalsIgnoreCase(request.linkedReferenceType())) {
            learningTaskRepository.findById(request.linkedReferenceId()).ifPresent(lt -> {
                if (lt.getPhase() != null && lt.getPhase().getRoadmap() != null) {
                    LocalDate deadline = lt.getPhase().getRoadmap().getTargetCompletionDate();
                    if (deadline != null && date.isAfter(deadline)) {
                        throw new InvalidPayloadException("Cannot schedule task on " + date + " as it exceeds the roadmap deadline of " + deadline + ".");
                    }
                }
            });
        }

        // 3. Daily Availability Cap
        LearningProfile profile = profileRepository.findByUser(user).orElse(null);
        int dailyAvailable = profile != null && profile.getDailyStudyMinutes() != null ? profile.getDailyStudyMinutes() : 180;
        int estMinutes = request.estimatedMinutes() != null && request.estimatedMinutes() > 0 ? request.estimatedMinutes() : 45;

        int currentScheduledMins = existingTasks.stream().mapToInt(PlannerTask::getEstimatedMinutes).sum();
        if (currentScheduledMins + estMinutes > dailyAvailable) {
            // Cap estimated minutes to remaining daily allowance if some time remains, otherwise min 15
            int remaining = dailyAvailable - currentScheduledMins;
            if (remaining > 15) {
                estMinutes = remaining;
            }
        }

        PlannerTask task = new PlannerTask(
                user,
                cleanTitle,
                request.description(),
                request.taskType() != null ? request.taskType() : PlannerTaskType.CUSTOM,
                date,
                request.priority() != null ? request.priority() : PlannerTaskPriority.MEDIUM,
                estMinutes,
                request.linkedReferenceId(),
                request.linkedReferenceType()
        );
        task.setNotes(request.notes());

        PlannerTask saved = plannerTaskRepository.save(task);
        return PlannerDtos.PlannerTaskResponse.fromEntity(saved);
    }

    @Transactional
    public PlannerDtos.PlannerTaskResponse updatePlannerTask(User user, Long taskId, PlannerDtos.UpdatePlannerTaskRequest request) {
        PlannerTask task = plannerTaskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with ID: " + taskId));

        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.taskType() != null) {
            task.setTaskType(request.taskType());
        }
        if (request.scheduledDate() != null) {
            task.setScheduledDate(request.scheduledDate());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.estimatedMinutes() != null && request.estimatedMinutes() > 0) {
            task.setEstimatedMinutes(request.estimatedMinutes());
        }
        if (request.actualMinutes() != null && request.actualMinutes() >= 0) {
            task.setActualMinutes(request.actualMinutes());
        }
        if (request.notes() != null) {
            task.setNotes(request.notes());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
            if (request.status() == PlannerTaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
                if (task.getActualMinutes() == 0) {
                    task.setActualMinutes(task.getEstimatedMinutes());
                }
            } else {
                task.setCompletedAt(null);
            }
        }

        PlannerTask saved = plannerTaskRepository.save(task);
        return PlannerDtos.PlannerTaskResponse.fromEntity(saved);
    }

    @Transactional
    public PlannerDtos.PlannerTaskResponse rescheduleTask(User user, Long taskId, PlannerDtos.RescheduleTaskRequest request) {
        PlannerTask task = plannerTaskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with ID: " + taskId));

        if (request.newDate() == null) {
            throw new InvalidPayloadException("New scheduled date is required for rescheduling.");
        }

        task.setScheduledDate(request.newDate());
        task.setStatus(PlannerTaskStatus.RESCHEDULED);
        if (request.reason() != null && !request.reason().isBlank()) {
            String existingNotes = task.getNotes() != null ? task.getNotes() + "\n" : "";
            task.setNotes(existingNotes + "Rescheduled: " + request.reason());
        }

        PlannerTask saved = plannerTaskRepository.save(task);
        return PlannerDtos.PlannerTaskResponse.fromEntity(saved);
    }

    @Transactional
    public void deletePlannerTask(User user, Long taskId) {
        PlannerTask task = plannerTaskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Planner task not found with ID: " + taskId));
        plannerTaskRepository.delete(task);
    }

    @Transactional
    public PlannerDtos.DailyPlanSummaryResponse autoPopulateTodayTasks(User user, LocalDate date) {
        LocalDate planDate = date != null ? date : LocalDate.now();

        // 1. Check if tasks already exist for this date
        long existingCount = plannerTaskRepository.countByUserAndScheduledDate(user, planDate);
        if (existingCount > 0) {
            return getDailyPlan(user, planDate);
        }

        LearningProfile profile = profileRepository.findByUser(user).orElse(null);
        int dailyAvailable = profile != null && profile.getDailyStudyMinutes() != null ? profile.getDailyStudyMinutes() : 90;
        int remainingMinutes = dailyAvailable;

        // 2. Fetch pending roadmap tasks
        List<LearningTask> pendingTasks = learningTaskRepository.findByUserAndStatus(user, TaskStatus.TODO);
        if (!pendingTasks.isEmpty() && remainingMinutes >= 30) {
            LearningTask topTask = pendingTasks.get(0);
            int taskMins = Math.min(remainingMinutes, topTask.getEstimatedMinutes() > 0 ? topTask.getEstimatedMinutes() : 45);
            remainingMinutes -= taskMins;

            PlannerTask roadmapPlanner = new PlannerTask(
                    user,
                    "Study: " + topTask.getTitle(),
                    "Complete learning task from active roadmap. Topics: " + (topTask.getSubtopics() != null ? topTask.getSubtopics() : topTask.getTopic()),
                    PlannerTaskType.ROADMAP_TOPIC,
                    planDate,
                    PlannerTaskPriority.HIGH,
                    taskMins,
                    topTask.getId(),
                    "LEARNING_TASK"
            );
            plannerTaskRepository.save(roadmapPlanner);
        }

        // 3. Fetch due spaced revisions
        List<SpacedRevisionItem> dueRevisions = spacedRevisionItemRepository.findByUserAndStatusOrderByScheduledDateAsc(user, RevisionStatus.DUE);
        if (!dueRevisions.isEmpty() && remainingMinutes >= 20) {
            SpacedRevisionItem rev = dueRevisions.get(0);
            int revMins = Math.min(remainingMinutes, 30);
            remainingMinutes -= revMins;

            PlannerTask revisionPlanner = new PlannerTask(
                    user,
                    "Spaced Revision: " + rev.getTitle(),
                    "Perform spaced repetition review (Stage " + rev.getStage() + ") and submit self-assessment rating.",
                    PlannerTaskType.REVISION,
                    planDate,
                    PlannerTaskPriority.HIGH,
                    revMins,
                    rev.getId(),
                    "SPACED_REVISION"
            );
            plannerTaskRepository.save(revisionPlanner);
        }

        // 4. Daily DSA Practice Task
        if (remainingMinutes >= 25) {
            int dsaMins = Math.min(remainingMinutes, 45);
            PlannerTask dsaPlanner = new PlannerTask(
                    user,
                    "Daily Algorithmic Practice (2 Problems)",
                    "Solve targeted Data Structures & Algorithms problems to maintain coding velocity.",
                    PlannerTaskType.DSA_PRACTICE,
                    planDate,
                    PlannerTaskPriority.MEDIUM,
                    dsaMins,
                    null,
                    "DSA_TRACKER"
            );
            plannerTaskRepository.save(dsaPlanner);
        }

        return getDailyPlan(user, planDate);
    }
}
