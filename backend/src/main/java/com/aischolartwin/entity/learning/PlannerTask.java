package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "planner_tasks",
        indexes = {
                @Index(name = "idx_planner_tasks_user_date_status", columnList = "user_id, scheduled_date, status")
        }
)
public class PlannerTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private PlannerTaskType taskType = PlannerTaskType.CUSTOM;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PlannerTaskPriority priority = PlannerTaskPriority.MEDIUM;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes = 45;

    @Column(name = "actual_minutes", nullable = false)
    private Integer actualMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlannerTaskStatus status = PlannerTaskStatus.PENDING;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "linked_reference_id")
    private Long linkedReferenceId;

    @Column(name = "linked_reference_type", length = 50)
    private String linkedReferenceType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlannerTask() {
    }

    public PlannerTask(
            User user,
            String title,
            String description,
            PlannerTaskType taskType,
            LocalDate scheduledDate,
            PlannerTaskPriority priority,
            Integer estimatedMinutes,
            Long linkedReferenceId,
            String linkedReferenceType
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.taskType = taskType != null ? taskType : PlannerTaskType.CUSTOM;
        this.scheduledDate = scheduledDate != null ? scheduledDate : LocalDate.now();
        this.priority = priority != null ? priority : PlannerTaskPriority.MEDIUM;
        this.estimatedMinutes = estimatedMinutes != null ? estimatedMinutes : 45;
        this.status = PlannerTaskStatus.PENDING;
        this.actualMinutes = 0;
        this.linkedReferenceId = linkedReferenceId;
        this.linkedReferenceType = linkedReferenceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlannerTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(PlannerTaskType taskType) {
        this.taskType = taskType;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public PlannerTaskPriority getPriority() {
        return priority;
    }

    public void setPriority(PlannerTaskPriority priority) {
        this.priority = priority;
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

    public PlannerTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PlannerTaskStatus status) {
        this.status = status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getLinkedReferenceId() {
        return linkedReferenceId;
    }

    public void setLinkedReferenceId(Long linkedReferenceId) {
        this.linkedReferenceId = linkedReferenceId;
    }

    public String getLinkedReferenceType() {
        return linkedReferenceType;
    }

    public void setLinkedReferenceType(String linkedReferenceType) {
        this.linkedReferenceType = linkedReferenceType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
