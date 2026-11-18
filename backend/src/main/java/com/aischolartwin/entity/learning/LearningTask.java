package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "learning_tasks",
        indexes = {
                @Index(name = "idx_learning_tasks_phase_order", columnList = "phase_id, task_order"),
                @Index(name = "idx_learning_tasks_user_status", columnList = "user_id, status")
        }
)
public class LearningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private RoadmapPhase phase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "subtopics", columnDefinition = "TEXT")
    private String subtopics;

    @Column(name = "task_order", nullable = false)
    private Integer taskOrder = 1;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes = 60;

    @Column(name = "actual_minutes", nullable = false)
    private Integer actualMinutes = 0;

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    @Column(name = "practical_exercises", columnDefinition = "TEXT")
    private String practicalExercises;

    @Column(name = "mini_project", columnDefinition = "TEXT")
    private String miniProject;

    @Column(name = "revision_checkpoints", columnDefinition = "TEXT")
    private String revisionCheckpoints;

    @Column(name = "completion_criteria", columnDefinition = "TEXT")
    private String completionCriteria;

    @Column(name = "prerequisite_task_id")
    private Long prerequisiteTaskId;

    @Column(name = "prerequisite_title", length = 150)
    private String prerequisiteTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningTask() {
    }

    public LearningTask(
            RoadmapPhase phase,
            User user,
            String title,
            String topic,
            String subtopics,
            Integer taskOrder,
            Integer estimatedMinutes,
            String learningObjectives,
            String practicalExercises,
            String completionCriteria
    ) {
        this.phase = phase;
        this.user = user;
        this.title = title;
        this.topic = topic;
        this.subtopics = subtopics;
        this.taskOrder = taskOrder;
        this.estimatedMinutes = estimatedMinutes != null ? estimatedMinutes : 60;
        this.learningObjectives = learningObjectives;
        this.practicalExercises = practicalExercises;
        this.completionCriteria = completionCriteria;
        this.status = TaskStatus.TODO;
        this.actualMinutes = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoadmapPhase getPhase() {
        return phase;
    }

    public void setPhase(RoadmapPhase phase) {
        this.phase = phase;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSubtopics() {
        return subtopics;
    }

    public void setSubtopics(String subtopics) {
        this.subtopics = subtopics;
    }

    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(Integer actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public String getPracticalExercises() {
        return practicalExercises;
    }

    public void setPracticalExercises(String practicalExercises) {
        this.practicalExercises = practicalExercises;
    }

    public String getMiniProject() {
        return miniProject;
    }

    public void setMiniProject(String miniProject) {
        this.miniProject = miniProject;
    }

    public String getRevisionCheckpoints() {
        return revisionCheckpoints;
    }

    public void setRevisionCheckpoints(String revisionCheckpoints) {
        this.revisionCheckpoints = revisionCheckpoints;
    }

    public String getCompletionCriteria() {
        return completionCriteria;
    }

    public void setCompletionCriteria(String completionCriteria) {
        this.completionCriteria = completionCriteria;
    }

    public Long getPrerequisiteTaskId() {
        return prerequisiteTaskId;
    }

    public void setPrerequisiteTaskId(Long prerequisiteTaskId) {
        this.prerequisiteTaskId = prerequisiteTaskId;
    }

    public String getPrerequisiteTitle() {
        return prerequisiteTitle;
    }

    public void setPrerequisiteTitle(String prerequisiteTitle) {
        this.prerequisiteTitle = prerequisiteTitle;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
