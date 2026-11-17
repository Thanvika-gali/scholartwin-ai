package com.aischolartwin.entity;

import jakarta.persistence.*;

/**
 * Mapping entity between a Tag and a Knowledge Resource (Document, Note, Link).
 */
@Entity
@Table(
        name = "knowledge_resource_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_krt_resource", columnNames = {"tag_id", "resource_type", "resource_id"})
        }
)
public class KnowledgeResourceTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private KnowledgeTag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    public KnowledgeResourceTag() {}

    public KnowledgeResourceTag(KnowledgeTag tag, ResourceType resourceType, Long resourceId) {
        this.tag = tag;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KnowledgeTag getTag() {
        return tag;
    }

    public void setTag(KnowledgeTag tag) {
        this.tag = tag;
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
}

