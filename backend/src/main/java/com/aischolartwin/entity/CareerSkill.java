package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "career_skills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_career_skills_user_skill", columnNames = {"user_id", "skill_name"})
}, indexes = {
        @Index(name = "idx_career_skills_user_category", columnList = "user_id, category"),
        @Index(name = "idx_career_skills_user_proficiency", columnList = "user_id, proficiency")
})
public class CareerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private CareerSkillCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 30)
    private SkillProficiency proficiency = SkillProficiency.INTERMEDIATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private SkillSource source = SkillSource.USER_ENTERED;

    @Column(name = "evidence_text", length = 500)
    private String evidenceText;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CareerSkill() {
    }

    public CareerSkill(User user, String skillName, CareerSkillCategory category,
                       SkillProficiency proficiency, SkillSource source, String evidenceText) {
        this.user = user;
        this.skillName = skillName;
        this.category = category;
        this.proficiency = proficiency != null ? proficiency : SkillProficiency.INTERMEDIATE;
        this.source = source != null ? source : SkillSource.USER_ENTERED;
        this.evidenceText = evidenceText;
        this.verified = source == SkillSource.RESUME || source == SkillSource.CERTIFICATION || source == SkillSource.PROJECT;
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

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public CareerSkillCategory getCategory() {
        return category;
    }

    public void setCategory(CareerSkillCategory category) {
        this.category = category;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public SkillSource getSource() {
        return source;
    }

    public void setSource(SkillSource source) {
        this.source = source;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

