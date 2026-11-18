package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "learning_goals",
        indexes = {
                @Index(name = "idx_learning_goals_user", columnList = "user_id, status")
        }
)
public class LearningGoal {

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

    @Column(name = "category", length = 50)
    private String category = "CAREER_ROLE";

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private PlannerTaskPriority priority = PlannerTaskPriority.HIGH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlannerTaskStatus status = PlannerTaskStatus.PENDING;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningGoal() {
    }

    public LearningGoal(User user, String title, String description, String category, PlannerTaskPriority priority, LocalDate targetDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : PlannerTaskPriority.HIGH;
        this.status = PlannerTaskStatus.PENDING;
        this.targetDate = targetDate;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public PlannerTaskPriority getPriority() {
        return priority;
    }

    public void setPriority(PlannerTaskPriority priority) {
        this.priority = priority;
    }

    public PlannerTaskStatus getStatus() {
        return status;
    }

    public void setStatus(PlannerTaskStatus status) {
        this.status = status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
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
