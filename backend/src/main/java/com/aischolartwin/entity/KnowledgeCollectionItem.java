package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Join entity mapping a resource (Document, Note, Link) into a Collection.
 */
@Entity
@Table(
        name = "knowledge_collection_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_kci_resource", columnNames = {"collection_id", "resource_type", "resource_id"})
        }
)
public class KnowledgeCollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private KnowledgeCollection collection;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public KnowledgeCollectionItem() {}

    public KnowledgeCollectionItem(KnowledgeCollection collection, ResourceType resourceType, Long resourceId) {
        this.collection = collection;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KnowledgeCollection getCollection() {
        return collection;
    }

    public void setCollection(KnowledgeCollection collection) {
        this.collection = collection;
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

    public Instant getAddedAt() {
        return addedAt;
    }
}

