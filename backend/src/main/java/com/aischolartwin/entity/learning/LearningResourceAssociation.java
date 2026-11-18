package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "learning_resource_associations",
        indexes = {
                @Index(name = "idx_resource_associations_user_task", columnList = "user_id, task_id")
        }
)
public class LearningResourceAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private LearningTask task;

    @Column(name = "topic", length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 30)
    private LearningResourceType resourceType = LearningResourceType.EXTERNAL_URL;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_studied", nullable = false)
    private Boolean studied = false;

    @Column(name = "studied_at")
    private Instant studiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningResourceAssociation() {
    }

    public LearningResourceAssociation(
            User user,
            LearningTask task,
            String topic,
            LearningResourceType resourceType,
            Long resourceId,
            String title,
            String url,
            String notes
    ) {
        this.user = user;
        this.task = task;
        this.topic = topic;
        this.resourceType = resourceType != null ? resourceType : LearningResourceType.EXTERNAL_URL;
        this.resourceId = resourceId;
        this.title = title;
        this.url = url;
        this.notes = notes;
        this.studied = false;
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

    public LearningTask getTask() {
        return task;
    }

    public void setTask(LearningTask task) {
        this.task = task;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LearningResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(LearningResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getStudied() {
        return studied;
    }

    public void setStudied(Boolean studied) {
        this.studied = studied;
    }

    public Instant getStudiedAt() {
        return studiedAt;
    }

    public void setStudiedAt(Instant studiedAt) {
        this.studiedAt = studiedAt;
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
