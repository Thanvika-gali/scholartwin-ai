package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing an AI or deterministic folder and metadata suggestion for an unorganized resource.
 */
@Entity
@Table(
        name = "knowledge_organization_suggestions",
        indexes = {
                @Index(name = "idx_kos_user_status", columnList = "user_id, status")
        }
)
public class KnowledgeOrganizationSuggestion {

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

    @Column(name = "suggested_folder_id")
    private Long suggestedFolderId;

    @Column(name = "suggested_folder_path", length = 255)
    private String suggestedFolderPath;

    @Column(name = "suggested_subject", length = 100)
    private String suggestedSubject;

    @Column(name = "suggested_semester")
    private Integer suggestedSemester;

    @Column(name = "suggested_category", length = 50)
    private String suggestedCategory;

    @Column(name = "suggested_tags_json", length = 1000)
    private String suggestedTagsJson;

    @Column(name = "confidence_score", nullable = false)
    private double confidenceScore = 0.0; // 0.0 to 1.0

    @Column(name = "reasoning", length = 500)
    private String reasoning;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public KnowledgeOrganizationSuggestion() {}

    public KnowledgeOrganizationSuggestion(User user, ResourceType resourceType, Long resourceId,
                                           Long suggestedFolderId, String suggestedFolderPath,
                                           String suggestedSubject, Integer suggestedSemester,
                                           String suggestedCategory, String suggestedTagsJson,
                                           double confidenceScore, String reasoning, boolean isAiGenerated) {
        this.user = user;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.suggestedFolderId = suggestedFolderId;
        this.suggestedFolderPath = suggestedFolderPath;
        this.suggestedSubject = suggestedSubject;
        this.suggestedSemester = suggestedSemester;
        this.suggestedCategory = suggestedCategory;
        this.suggestedTagsJson = suggestedTagsJson;
        this.confidenceScore = confidenceScore;
        this.reasoning = reasoning;
        this.isAiGenerated = isAiGenerated;
        this.status = SuggestionStatus.PENDING;
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

    public Long getSuggestedFolderId() {
        return suggestedFolderId;
    }

    public void setSuggestedFolderId(Long suggestedFolderId) {
        this.suggestedFolderId = suggestedFolderId;
    }

    public String getSuggestedFolderPath() {
        return suggestedFolderPath;
    }

    public void setSuggestedFolderPath(String suggestedFolderPath) {
        this.suggestedFolderPath = suggestedFolderPath;
    }

    public String getSuggestedSubject() {
        return suggestedSubject;
    }

    public void setSuggestedSubject(String suggestedSubject) {
        this.suggestedSubject = suggestedSubject;
    }

    public Integer getSuggestedSemester() {
        return suggestedSemester;
    }

    public void setSuggestedSemester(Integer suggestedSemester) {
        this.suggestedSemester = suggestedSemester;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public void setSuggestedCategory(String suggestedCategory) {
        this.suggestedCategory = suggestedCategory;
    }

    public String getSuggestedTagsJson() {
        return suggestedTagsJson;
    }

    public void setSuggestedTagsJson(String suggestedTagsJson) {
        this.suggestedTagsJson = suggestedTagsJson;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public void setStatus(SuggestionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

