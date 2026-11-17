package com.aischolartwin.entity;

import jakarta.persistence.*;

/**
 * Entity representing an academic knowledge tag.
 */
@Entity
@Table(
        name = "knowledge_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_kt_user_name", columnNames = {"user_id", "name"})
        }
)
public class KnowledgeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", length = 30)
    private String color = "#178582";

    public KnowledgeTag() {}

    public KnowledgeTag(User user, String name, String color) {
        this.user = user;
        this.name = name != null ? name.trim().toLowerCase() : "";
        this.color = (color != null && !color.isBlank()) ? color.trim() : "#178582";
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
        this.name = name != null ? name.trim().toLowerCase() : "";
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

