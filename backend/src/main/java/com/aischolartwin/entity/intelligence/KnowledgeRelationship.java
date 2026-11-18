package com.aischolartwin.entity.intelligence;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "ai_knowledge_relationships",
        indexes = {
                @Index(name = "idx_krel_user", columnList = "user_id"),
                @Index(name = "idx_krel_source", columnList = "source_node_id"),
                @Index(name = "idx_krel_target", columnList = "target_node_id")
        }
)
public class KnowledgeRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_node_id", nullable = false)
    private KnowledgeNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_node_id", nullable = false)
    private KnowledgeNode targetNode;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private RelationType relationType = RelationType.SUPPORTS;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "strength", nullable = false)
    private Double strength = 0.8;

    @Column(name = "evidence_source", length = 100)
    private String evidenceSource;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public KnowledgeRelationship() {
    }

    public KnowledgeRelationship(User user, KnowledgeNode sourceNode, KnowledgeNode targetNode,
                                 RelationType relationType, String description,
                                 Double strength, String evidenceSource) {
        this.user = user;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.relationType = relationType != null ? relationType : RelationType.SUPPORTS;
        this.description = description;
        this.strength = strength != null ? strength : 0.8;
        this.evidenceSource = evidenceSource;
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

    public KnowledgeNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(KnowledgeNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public KnowledgeNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(KnowledgeNode targetNode) {
        this.targetNode = targetNode;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getStrength() {
        return strength;
    }

    public void setStrength(Double strength) {
        this.strength = strength;
    }

    public String getEvidenceSource() {
        return evidenceSource;
    }

    public void setEvidenceSource(String evidenceSource) {
        this.evidenceSource = evidenceSource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
