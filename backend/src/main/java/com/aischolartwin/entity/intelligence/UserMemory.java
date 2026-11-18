package com.aischolartwin.entity.intelligence;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_memories",
        indexes = {
                @Index(name = "idx_memories_user_status", columnList = "user_id, status"),
                @Index(name = "idx_memories_user_cat_status", columnList = "user_id, category, status"),
                @Index(name = "idx_memories_user_pinned", columnList = "user_id, is_pinned")
        }
)
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private MemoryCategory category;

    @Column(name = "summary", length = 255)
    private String summary;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_type", nullable = false, length = 30)
    private MemoryType memoryType = MemoryType.FACT;

    @Enumerated(EnumType.STRING)
    @Column(name = "provenance", nullable = false, length = 30)
    private MemoryProvenance provenance = MemoryProvenance.INFERRED_FROM_ACTIVITY;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private MemorySourceType sourceType = MemorySourceType.MANUAL_ENTRY;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Column(name = "confidence", nullable = false)
    private Double confidence = 0.85;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    private MemoryImportance importance = MemoryImportance.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemoryStatus status = MemoryStatus.ACTIVE;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserMemory() {
    }

    public UserMemory(User user, MemoryCategory category, String summary, String content,
                      MemoryType memoryType, MemoryProvenance provenance,
                      MemorySourceType sourceType, String sourceReference,
                      Double confidence, MemoryImportance importance) {
        this.user = user;
        this.category = category;
        this.summary = summary;
        this.content = content;
        this.memoryType = memoryType != null ? memoryType : MemoryType.FACT;
        this.provenance = provenance != null ? provenance : MemoryProvenance.INFERRED_FROM_ACTIVITY;
        this.sourceType = sourceType != null ? sourceType : MemorySourceType.MANUAL_ENTRY;
        this.sourceReference = sourceReference;
        this.confidence = confidence != null ? confidence : 0.85;
        this.importance = importance != null ? importance : MemoryImportance.MEDIUM;
        this.status = MemoryStatus.ACTIVE;
        this.isPinned = false;
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

    public MemoryCategory getCategory() {
        return category;
    }

    public void setCategory(MemoryCategory category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    public MemoryProvenance getProvenance() {
        return provenance;
    }

    public void setProvenance(MemoryProvenance provenance) {
        this.provenance = provenance;
    }

    public MemorySourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(MemorySourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public MemoryImportance getImportance() {
        return importance;
    }

    public void setImportance(MemoryImportance importance) {
        this.importance = importance;
    }

    public MemoryStatus getStatus() {
        return status;
    }

    public void setStatus(MemoryStatus status) {
        this.status = status;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(Instant lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
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
