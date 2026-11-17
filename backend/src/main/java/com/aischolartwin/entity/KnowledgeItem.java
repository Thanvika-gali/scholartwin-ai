package com.aischolartwin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA Entity representing a personalized Knowledge Item (Note or Link)
 * saved by an academic user in the Knowledge Hub.
 */
@Entity
@Table(
    name = "knowledge_items",
    indexes = {
        @Index(name = "idx_ki_user_id", columnList = "user_id"),
        @Index(name = "idx_ki_user_updated", columnList = "user_id, updated_at"),
        @Index(name = "idx_ki_user_type", columnList = "user_id, type"),
        @Index(name = "idx_ki_user_category", columnList = "user_id, category")
    }
)
public class KnowledgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private KnowledgeItemType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "connected_document_id")
    private Long connectedDocumentId;

    @Column(name = "connected_page_number")
    private Integer connectedPageNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public KnowledgeItem() {
    }

    public KnowledgeItem(User user, KnowledgeItemType type, String title, String content, String url, String description, String category) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.url = url;
        this.description = description;
        this.category = category != null && !category.isBlank() ? category.trim() : "General";
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.category == null || this.category.isBlank()) {
            this.category = "General";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.category == null || this.category.isBlank()) {
            this.category = "General";
        }
    }

    // Getters and Setters

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

    public KnowledgeItemType getType() {
        return type;
    }

    public void setType(KnowledgeItemType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        this.category = category != null && !category.isBlank() ? category.trim() : "General";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(LocalDateTime lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }

    public Long getConnectedDocumentId() {
        return connectedDocumentId;
    }

    public void setConnectedDocumentId(Long connectedDocumentId) {
        this.connectedDocumentId = connectedDocumentId;
    }

    public Integer getConnectedPageNumber() {
        return connectedPageNumber;
    }

    public void setConnectedPageNumber(Integer connectedPageNumber) {
        this.connectedPageNumber = connectedPageNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeItem that = (KnowledgeItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KnowledgeItem{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

