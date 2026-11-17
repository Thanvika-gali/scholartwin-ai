package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "dsa_revisions",
        indexes = {
                @Index(name = "idx_dsa_revisions_user_status_date", columnList = "user_id, status, scheduled_date"),
                @Index(name = "idx_dsa_revisions_problem", columnList = "problem_id, scheduled_date")
        }
)
public class DSARevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private DSAProblem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "revision_notes", columnDefinition = "TEXT")
    private String revisionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DSARevisionStatus status = DSARevisionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DSARevision() {
    }

    public DSARevision(DSAProblem problem, User user, LocalDate scheduledDate, String revisionNotes) {
        this.problem = problem;
        this.user = user;
        this.scheduledDate = scheduledDate != null ? scheduledDate : LocalDate.now().plusDays(3);
        this.revisionNotes = revisionNotes;
        this.status = DSARevisionStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DSAProblem getProblem() {
        return problem;
    }

    public void setProblem(DSAProblem problem) {
        this.problem = problem;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getRevisionNotes() {
        return revisionNotes;
    }

    public void setRevisionNotes(String revisionNotes) {
        this.revisionNotes = revisionNotes;
    }

    public DSARevisionStatus getStatus() {
        return status;
    }

    public void setStatus(DSARevisionStatus status) {
        this.status = status;
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

