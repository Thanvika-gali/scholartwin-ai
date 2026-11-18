package com.aischolartwin.entity.intelligence;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "memory_preferences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_memory_pref_user", columnNames = "user_id")
        }
)
public class MemoryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "memory_enabled", nullable = false)
    private Boolean memoryEnabled = true;

    @Column(name = "learning_memory_enabled", nullable = false)
    private Boolean learningMemoryEnabled = true;

    @Column(name = "technical_memory_enabled", nullable = false)
    private Boolean technicalMemoryEnabled = true;

    @Column(name = "career_memory_enabled", nullable = false)
    private Boolean careerMemoryEnabled = true;

    @Column(name = "knowledge_memory_enabled", nullable = false)
    private Boolean knowledgeMemoryEnabled = true;

    @Column(name = "interaction_memory_enabled", nullable = false)
    private Boolean interactionMemoryEnabled = true;

    @Column(name = "auto_extract_from_activity", nullable = false)
    private Boolean autoExtractFromActivity = true;

    @Column(name = "auto_extract_from_chat", nullable = false)
    private Boolean autoExtractFromChat = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MemoryPreference() {
    }

    public MemoryPreference(User user) {
        this.user = user;
        this.memoryEnabled = true;
        this.learningMemoryEnabled = true;
        this.technicalMemoryEnabled = true;
        this.careerMemoryEnabled = true;
        this.knowledgeMemoryEnabled = true;
        this.interactionMemoryEnabled = true;
        this.autoExtractFromActivity = true;
        this.autoExtractFromChat = true;
    }

    public boolean isCategoryEnabled(MemoryCategory category) {
        if (!Boolean.TRUE.equals(memoryEnabled)) {
            return false;
        }
        return switch (category) {
            case LEARNING -> Boolean.TRUE.equals(learningMemoryEnabled);
            case TECHNICAL -> Boolean.TRUE.equals(technicalMemoryEnabled);
            case CAREER -> Boolean.TRUE.equals(careerMemoryEnabled);
            case KNOWLEDGE -> Boolean.TRUE.equals(knowledgeMemoryEnabled);
            case INTERACTION -> Boolean.TRUE.equals(interactionMemoryEnabled);
        };
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

    public Boolean getMemoryEnabled() {
        return memoryEnabled;
    }

    public void setMemoryEnabled(Boolean memoryEnabled) {
        this.memoryEnabled = memoryEnabled;
    }

    public Boolean getLearningMemoryEnabled() {
        return learningMemoryEnabled;
    }

    public void setLearningMemoryEnabled(Boolean learningMemoryEnabled) {
        this.learningMemoryEnabled = learningMemoryEnabled;
    }

    public Boolean getTechnicalMemoryEnabled() {
        return technicalMemoryEnabled;
    }

    public void setTechnicalMemoryEnabled(Boolean technicalMemoryEnabled) {
        this.technicalMemoryEnabled = technicalMemoryEnabled;
    }

    public Boolean getCareerMemoryEnabled() {
        return careerMemoryEnabled;
    }

    public void setCareerMemoryEnabled(Boolean careerMemoryEnabled) {
        this.careerMemoryEnabled = careerMemoryEnabled;
    }

    public Boolean getKnowledgeMemoryEnabled() {
        return knowledgeMemoryEnabled;
    }

    public void setKnowledgeMemoryEnabled(Boolean knowledgeMemoryEnabled) {
        this.knowledgeMemoryEnabled = knowledgeMemoryEnabled;
    }

    public Boolean getInteractionMemoryEnabled() {
        return interactionMemoryEnabled;
    }

    public void setInteractionMemoryEnabled(Boolean interactionMemoryEnabled) {
        this.interactionMemoryEnabled = interactionMemoryEnabled;
    }

    public Boolean getAutoExtractFromActivity() {
        return autoExtractFromActivity;
    }

    public void setAutoExtractFromActivity(Boolean autoExtractFromActivity) {
        this.autoExtractFromActivity = autoExtractFromActivity;
    }

    public Boolean getAutoExtractFromChat() {
        return autoExtractFromChat;
    }

    public void setAutoExtractFromChat(Boolean autoExtractFromChat) {
        this.autoExtractFromChat = autoExtractFromChat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
