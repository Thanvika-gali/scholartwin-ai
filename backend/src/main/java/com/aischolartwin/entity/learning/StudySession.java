package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "study_sessions",
        indexes = {
                @Index(name = "idx_study_sessions_user_date", columnList = "user_id, session_date")
        }
)
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "category", nullable = false, length = 50)
    private String category = "ROADMAP_STUDY";

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "linked_roadmap_id")
    private Long linkedRoadmapId;

    @Column(name = "linked_task_id")
    private Long linkedTaskId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public StudySession() {
    }

    public StudySession(User user, LocalDate sessionDate, Integer durationMinutes, String category, String title, String notes) {
        this.user = user;
        this.sessionDate = sessionDate != null ? sessionDate : LocalDate.now();
        this.durationMinutes = durationMinutes != null ? durationMinutes : 30;
        this.category = category != null ? category : "ROADMAP_STUDY";
        this.title = title;
        this.notes = notes;
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

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getLinkedRoadmapId() {
        return linkedRoadmapId;
    }

    public void setLinkedRoadmapId(Long linkedRoadmapId) {
        this.linkedRoadmapId = linkedRoadmapId;
    }

    public Long getLinkedTaskId() {
        return linkedTaskId;
    }

    public void setLinkedTaskId(Long linkedTaskId) {
        this.linkedTaskId = linkedTaskId;
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
