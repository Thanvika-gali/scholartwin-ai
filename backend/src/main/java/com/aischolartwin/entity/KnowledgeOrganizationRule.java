package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing an automated user-defined organization rule.
 */
@Entity
@Table(
        name = "knowledge_organization_rules",
        indexes = {
                @Index(name = "idx_kor_user_enabled", columnList = "user_id, is_enabled, priority DESC")
        }
)
public class KnowledgeOrganizationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rule_name", nullable = false, length = 150)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_name", nullable = false, length = 50)
    private RuleField fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 30)
    private RuleOperator operator;

    @Column(name = "condition_value", nullable = false, length = 255)
    private String conditionValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_folder_id", nullable = false)
    private KnowledgeFolder targetFolder;

    @Column(name = "suggested_tags", length = 255)
    private String suggestedTags;

    @Column(name = "auto_move", nullable = false)
    private boolean autoMove = false;

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public KnowledgeOrganizationRule() {}

    public KnowledgeOrganizationRule(User user, String ruleName, RuleField fieldName, RuleOperator operator,
                                     String conditionValue, KnowledgeFolder targetFolder, String suggestedTags,
                                     boolean autoMove, int priority, boolean isEnabled) {
        this.user = user;
        this.ruleName = ruleName;
        this.fieldName = fieldName;
        this.operator = operator;
        this.conditionValue = conditionValue;
        this.targetFolder = targetFolder;
        this.suggestedTags = suggestedTags;
        this.autoMove = autoMove;
        this.priority = priority;
        this.isEnabled = isEnabled;
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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleField getFieldName() {
        return fieldName;
    }

    public void setFieldName(RuleField fieldName) {
        this.fieldName = fieldName;
    }

    public RuleOperator getOperator() {
        return operator;
    }

    public void setOperator(RuleOperator operator) {
        this.operator = operator;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public KnowledgeFolder getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(KnowledgeFolder targetFolder) {
        this.targetFolder = targetFolder;
    }

    public String getSuggestedTags() {
        return suggestedTags;
    }

    public void setSuggestedTags(String suggestedTags) {
        this.suggestedTags = suggestedTags;
    }

    public boolean isAutoMove() {
        return autoMove;
    }

    public void setAutoMove(boolean autoMove) {
        this.autoMove = autoMove;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

