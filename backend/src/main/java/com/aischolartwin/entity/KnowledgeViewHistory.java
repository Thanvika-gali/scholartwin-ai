package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity tracking resource view access history for recent activity feeds and analytics.
 */
@Entity
@Table(
        name = "knowledge_view_history",
        indexes = {
                @Index(name = "idx_kvh_user_viewed", columnList = "user_id, viewed_at DESC")
        }
)
public class KnowledgeViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @CreationTimestamp
    @Column(name = "viewed_at", nullable = false, updatable = false)
    private Instant viewedAt;

    public KnowledgeViewHistory() {}

    public KnowledgeViewHistory(User user, ResourceType resourceType, Long resourceId) {
        this.user = user;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
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

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }
}

