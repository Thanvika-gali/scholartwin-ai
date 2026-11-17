package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "career_actions", indexes = {
        @Index(name = "idx_career_actions_user_status", columnList = "user_id, status"),
        @Index(name = "idx_career_actions_user_priority", columnList = "user_id, priority"),
        @Index(name = "idx_career_actions_user_deadline", columnList = "user_id, suggested_deadline")
})
public class CareerAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reason_and_evidence", columnDefinition = "TEXT")
    private String reasonAndEvidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private CareerActionPriority priority = CareerActionPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CareerActionCategory category = CareerActionCategory.GENERAL;

    @Column(name = "related_skill_or_role", length = 100)
    private String relatedSkillOrRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CareerActionStatus status = CareerActionStatus.NOT_STARTED;

    @Column(name = "suggested_deadline")
    private LocalDate suggestedDeadline;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CareerAction() {
    }

    public CareerAction(User user, String title, String description, String reasonAndEvidence,
                        CareerActionPriority priority, CareerActionCategory category,
                        String relatedSkillOrRole, LocalDate suggestedDeadline) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.reasonAndEvidence = reasonAndEvidence;
        this.priority = priority != null ? priority : CareerActionPriority.MEDIUM;
        this.category = category != null ? category : CareerActionCategory.GENERAL;
        this.relatedSkillOrRole = relatedSkillOrRole;
        this.suggestedDeadline = suggestedDeadline;
        this.status = CareerActionStatus.NOT_STARTED;
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

    public String getReasonAndEvidence() {
        return reasonAndEvidence;
    }

    public void setReasonAndEvidence(String reasonAndEvidence) {
        this.reasonAndEvidence = reasonAndEvidence;
    }

    public CareerActionPriority getPriority() {
        return priority;
    }

    public void setPriority(CareerActionPriority priority) {
        this.priority = priority;
    }

    public CareerActionCategory getCategory() {
        return category;
    }

    public void setCategory(CareerActionCategory category) {
        this.category = category;
    }

    public String getRelatedSkillOrRole() {
        return relatedSkillOrRole;
    }

    public void setRelatedSkillOrRole(String relatedSkillOrRole) {
        this.relatedSkillOrRole = relatedSkillOrRole;
    }

    public CareerActionStatus getStatus() {
        return status;
    }

    public void setStatus(CareerActionStatus status) {
        this.status = status;
    }

    public LocalDate getSuggestedDeadline() {
        return suggestedDeadline;
    }

    public void setSuggestedDeadline(LocalDate suggestedDeadline) {
        this.suggestedDeadline = suggestedDeadline;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

