package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "revision_attempts",
        indexes = {
                @Index(name = "idx_revision_attempts_item", columnList = "revision_item_id, attempted_at")
        }
)
public class RevisionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_item_id", nullable = false)
    private SpacedRevisionItem revisionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false, length = 30)
    private RevisionRating rating;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "previous_interval_days")
    private Integer previousIntervalDays;

    @Column(name = "new_interval_days")
    private Integer newIntervalDays;

    @Column(name = "previous_stage")
    private Integer previousStage;

    @Column(name = "new_stage")
    private Integer newStage;

    @Column(name = "next_scheduled_date")
    private LocalDate nextScheduledDate;

    @Column(name = "reflection_notes", columnDefinition = "TEXT")
    private String reflectionNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RevisionAttempt() {
    }

    public RevisionAttempt(
            SpacedRevisionItem revisionItem,
            User user,
            RevisionRating rating,
            Integer previousIntervalDays,
            Integer newIntervalDays,
            Integer previousStage,
            Integer newStage,
            LocalDate nextScheduledDate,
            String reflectionNotes
    ) {
        this.revisionItem = revisionItem;
        this.user = user;
        this.rating = rating;
        this.attemptedAt = Instant.now();
        this.previousIntervalDays = previousIntervalDays;
        this.newIntervalDays = newIntervalDays;
        this.previousStage = previousStage;
        this.newStage = newStage;
        this.nextScheduledDate = nextScheduledDate;
        this.reflectionNotes = reflectionNotes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SpacedRevisionItem getRevisionItem() {
        return revisionItem;
    }

    public void setRevisionItem(SpacedRevisionItem revisionItem) {
        this.revisionItem = revisionItem;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RevisionRating getRating() {
        return rating;
    }

    public void setRating(RevisionRating rating) {
        this.rating = rating;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Instant attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public Integer getPreviousIntervalDays() {
        return previousIntervalDays;
    }

    public void setPreviousIntervalDays(Integer previousIntervalDays) {
        this.previousIntervalDays = previousIntervalDays;
    }

    public Integer getNewIntervalDays() {
        return newIntervalDays;
    }

    public void setNewIntervalDays(Integer newIntervalDays) {
        this.newIntervalDays = newIntervalDays;
    }

    public Integer getPreviousStage() {
        return previousStage;
    }

    public void setPreviousStage(Integer previousStage) {
        this.previousStage = previousStage;
    }

    public Integer getNewStage() {
        return newStage;
    }

    public void setNewStage(Integer newStage) {
        this.newStage = newStage;
    }

    public LocalDate getNextScheduledDate() {
        return nextScheduledDate;
    }

    public void setNextScheduledDate(LocalDate nextScheduledDate) {
        this.nextScheduledDate = nextScheduledDate;
    }

    public String getReflectionNotes() {
        return reflectionNotes;
    }

    public void setReflectionNotes(String reflectionNotes) {
        this.reflectionNotes = reflectionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
