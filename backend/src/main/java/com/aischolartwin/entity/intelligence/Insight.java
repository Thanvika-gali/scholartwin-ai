package com.aischolartwin.entity.intelligence;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ai_insights",
        indexes = {
                @Index(name = "idx_insights_user_status", columnList = "user_id, status"),
                @Index(name = "idx_insights_user_cat", columnList = "user_id, category")
        }
)
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private InsightCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false, length = 20)
    private InsightImportance importance = InsightImportance.MEDIUM;

    @Column(name = "confidence", nullable = false)
    private Double confidence = 0.85;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "action_module", length = 50)
    private String actionModule;

    @Column(name = "action_payload_json", columnDefinition = "TEXT")
    private String actionPayloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InsightStatus status = InsightStatus.ACTIVE;

    @OneToMany(mappedBy = "insight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsightEvidence> evidenceList = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private Instant generatedAt;

    public Insight() {
    }

    public Insight(User user, String title, String description, InsightCategory category,
                   InsightImportance importance, Double confidence,
                   String recommendedAction, String actionModule, String actionPayloadJson) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.category = category;
        this.importance = importance != null ? importance : InsightImportance.MEDIUM;
        this.confidence = confidence != null ? confidence : 0.85;
        this.recommendedAction = recommendedAction;
        this.actionModule = actionModule;
        this.actionPayloadJson = actionPayloadJson;
        this.status = InsightStatus.ACTIVE;
    }

    public void addEvidence(InsightEvidence evidence) {
        evidenceList.add(evidence);
        evidence.setInsight(this);
    }

    public void removeEvidence(InsightEvidence evidence) {
        evidenceList.remove(evidence);
        evidence.setInsight(null);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InsightCategory getCategory() {
        return category;
    }

    public void setCategory(InsightCategory category) {
        this.category = category;
    }

    public InsightImportance getImportance() {
        return importance;
    }

    public void setImportance(InsightImportance importance) {
        this.importance = importance;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public String getActionModule() {
        return actionModule;
    }

    public void setActionModule(String actionModule) {
        this.actionModule = actionModule;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }

    public InsightStatus getStatus() {
        return status;
    }

    public void setStatus(InsightStatus status) {
        this.status = status;
    }

    public List<InsightEvidence> getEvidenceList() {
        return evidenceList;
    }

    public void setEvidenceList(List<InsightEvidence> newEvidence) {
        this.evidenceList.clear();
        if (newEvidence != null) {
            this.evidenceList.addAll(newEvidence);
        }
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
