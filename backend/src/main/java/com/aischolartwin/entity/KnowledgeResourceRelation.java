package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity representing an interconnected relation between two academic resources.
 */
@Entity
@Table(
        name = "knowledge_resource_relations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_krr_edge", columnNames = {"user_id", "source_type", "source_id", "target_type", "target_id"})
        }
)
public class KnowledgeResourceRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private ResourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ResourceType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "relation_type", length = 50)
    private String relationType = "RELATED";

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "is_manual", nullable = false)
    private boolean isManual = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public KnowledgeResourceRelation() {}

    public KnowledgeResourceRelation(User user, ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId, String relationType, String reason, boolean isManual) {
        this.user = user;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.relationType = (relationType != null && !relationType.isBlank()) ? relationType.trim() : "RELATED";
        this.reason = reason;
        this.isManual = isManual;
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

    public ResourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ResourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public ResourceType getTargetType() {
        return targetType;
    }

    public void setTargetType(ResourceType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isManual() {
        return isManual;
    }

    public void setManual(boolean manual) {
        isManual = manual;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

