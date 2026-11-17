package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a hierarchical academic knowledge folder.
 */
@Entity
@Table(
        name = "knowledge_folders",
        indexes = {
                @Index(name = "idx_kf_user_parent", columnList = "user_id, parent_id")
        }
)
public class KnowledgeFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private KnowledgeFolder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<KnowledgeFolder> children = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 50)
    private String icon = "folder";

    @Column(name = "color", length = 30)
    private String color = "#178582";

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public KnowledgeFolder() {}

    public KnowledgeFolder(User user, KnowledgeFolder parent, String name, String description, String icon, String color) {
        this.user = user;
        this.parent = parent;
        this.name = name != null ? name.trim() : "Untitled Folder";
        this.description = description != null ? description.trim() : null;
        this.icon = (icon != null && !icon.isBlank()) ? icon.trim() : "folder";
        this.color = (color != null && !color.isBlank()) ? color.trim() : "#178582";
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

    public KnowledgeFolder getParent() {
        return parent;
    }

    public void setParent(KnowledgeFolder parent) {
        this.parent = parent;
    }

    public List<KnowledgeFolder> getChildren() {
        return children;
    }

    public void setChildren(List<KnowledgeFolder> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "Untitled Folder";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

