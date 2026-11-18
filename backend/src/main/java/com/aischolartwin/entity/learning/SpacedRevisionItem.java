package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "spaced_revision_items",
        indexes = {
                @Index(name = "idx_spaced_revisions_user_date_status", columnList = "user_id, scheduled_date, status")
        }
)
public class SpacedRevisionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private RevisionItemType itemType = RevisionItemType.CUSTOM_TOPIC;

    @Column(name = "linked_reference_id")
    private Long linkedReferenceId;

    @Column(name = "stage", nullable = false)
    private Integer stage = 0;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 1;

    @Column(name = "custom_interval_sequence", length = 100)
    private String customIntervalSequence = "1,3,7,14,30";

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RevisionStatus status = RevisionStatus.UPCOMING;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_rating", length = 30)
    private RevisionRating lastRating;

    @Column(name = "consecutive_success_count", nullable = false)
    private Integer consecutiveSuccessCount = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "revisionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("attemptedAt DESC")
    private List<RevisionAttempt> attempts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SpacedRevisionItem() {
    }

    public SpacedRevisionItem(
            User user,
            String title,
            RevisionItemType itemType,
            Long linkedReferenceId,
            LocalDate scheduledDate,
            String customIntervalSequence
    ) {
        this.user = user;
        this.title = title;
        this.itemType = itemType != null ? itemType : RevisionItemType.CUSTOM_TOPIC;
        this.linkedReferenceId = linkedReferenceId;
        this.scheduledDate = scheduledDate != null ? scheduledDate : LocalDate.now().plusDays(1);
        this.customIntervalSequence = customIntervalSequence != null ? customIntervalSequence : "1,3,7,14,30";
        this.stage = 0;
        this.intervalDays = 1;
        this.status = RevisionStatus.UPCOMING;
        this.consecutiveSuccessCount = 0;
    }

    public void addAttempt(RevisionAttempt attempt) {
        attempts.add(attempt);
        attempt.setRevisionItem(this);
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

    public RevisionItemType getItemType() {
        return itemType;
    }

    public void setItemType(RevisionItemType itemType) {
        this.itemType = itemType;
    }

    public Long getLinkedReferenceId() {
        return linkedReferenceId;
    }

    public void setLinkedReferenceId(Long linkedReferenceId) {
        this.linkedReferenceId = linkedReferenceId;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }

    public String getCustomIntervalSequence() {
        return customIntervalSequence;
    }

    public void setCustomIntervalSequence(String customIntervalSequence) {
        this.customIntervalSequence = customIntervalSequence;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public RevisionStatus getStatus() {
        return status;
    }

    public void setStatus(RevisionStatus status) {
        this.status = status;
    }

    public RevisionRating getLastRating() {
        return lastRating;
    }

    public void setLastRating(RevisionRating lastRating) {
        this.lastRating = lastRating;
    }

    public Integer getConsecutiveSuccessCount() {
        return consecutiveSuccessCount;
    }

    public void setConsecutiveSuccessCount(Integer consecutiveSuccessCount) {
        this.consecutiveSuccessCount = consecutiveSuccessCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<RevisionAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<RevisionAttempt> attempts) {
        this.attempts = attempts;
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
