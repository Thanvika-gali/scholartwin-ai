package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a user-defined collection of cross-folder resources.
 */
@Entity
@Table(
        name = "knowledge_collections",
        indexes = {
                @Index(name = "idx_kc_user", columnList = "user_id")
        }
)
public class KnowledgeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "color", length = 30)
    private String color = "#178582";

    @Column(name = "icon", length = 50)
    private String icon = "collection";

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KnowledgeCollectionItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public KnowledgeCollection() {}

    public KnowledgeCollection(User user, String name, String description, String color, String icon) {
        this.user = user;
        this.name = name != null ? name.trim() : "Untitled Collection";
        this.description = description != null ? description.trim() : null;
        this.color = (color != null && !color.isBlank()) ? color.trim() : "#178582";
        this.icon = (icon != null && !icon.isBlank()) ? icon.trim() : "collection";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<KnowledgeCollectionItem> getItems() {
        return items;
    }

    public void setItems(List<KnowledgeCollectionItem> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

