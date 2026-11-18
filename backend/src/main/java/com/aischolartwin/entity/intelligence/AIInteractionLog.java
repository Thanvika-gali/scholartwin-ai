package com.aischolartwin.entity.intelligence;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "ai_interaction_logs",
        indexes = {
                @Index(name = "idx_ai_logs_user_created", columnList = "user_id, created_at")
        }
)
public class AIInteractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "query_text", nullable = false, columnDefinition = "TEXT")
    private String query;

    @Column(name = "response_text", nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(name = "intent", length = 100)
    private String intent;

    @Column(name = "used_memory_ids_json", columnDefinition = "TEXT")
    private String usedMemoryIdsJson;

    @Column(name = "used_knowledge_item_ids_json", columnDefinition = "TEXT")
    private String usedKnowledgeItemIdsJson;

    @Column(name = "grounding_mode", length = 50)
    private String groundingMode = "MEMORY_GROUNDED";

    @Column(name = "candidate_memories_extracted_count")
    private Integer candidateMemoriesExtractedCount = 0;

    @Column(name = "latency_ms")
    private Long latencyMs = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AIInteractionLog() {
    }

    public AIInteractionLog(User user, String query, String response, String intent,
                            String usedMemoryIdsJson, String usedKnowledgeItemIdsJson,
                            String groundingMode, Integer candidateMemoriesExtractedCount, Long latencyMs) {
        this.user = user;
        this.query = query;
        this.response = response;
        this.intent = intent;
        this.usedMemoryIdsJson = usedMemoryIdsJson;
        this.usedKnowledgeItemIdsJson = usedKnowledgeItemIdsJson;
        this.groundingMode = groundingMode != null ? groundingMode : "MEMORY_GROUNDED";
        this.candidateMemoriesExtractedCount = candidateMemoriesExtractedCount != null ? candidateMemoriesExtractedCount : 0;
        this.latencyMs = latencyMs != null ? latencyMs : 0L;
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getUsedMemoryIdsJson() {
        return usedMemoryIdsJson;
    }

    public void setUsedMemoryIdsJson(String usedMemoryIdsJson) {
        this.usedMemoryIdsJson = usedMemoryIdsJson;
    }

    public String getUsedKnowledgeItemIdsJson() {
        return usedKnowledgeItemIdsJson;
    }

    public void setUsedKnowledgeItemIdsJson(String usedKnowledgeItemIdsJson) {
        this.usedKnowledgeItemIdsJson = usedKnowledgeItemIdsJson;
    }

    public String getGroundingMode() {
        return groundingMode;
    }

    public void setGroundingMode(String groundingMode) {
        this.groundingMode = groundingMode;
    }

    public Integer getCandidateMemoriesExtractedCount() {
        return candidateMemoriesExtractedCount;
    }

    public void setCandidateMemoriesExtractedCount(Integer candidateMemoriesExtractedCount) {
        this.candidateMemoriesExtractedCount = candidateMemoriesExtractedCount;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
