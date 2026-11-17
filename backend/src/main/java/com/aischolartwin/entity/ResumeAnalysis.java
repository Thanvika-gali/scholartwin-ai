package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "resume_analyses", indexes = {
        @Index(name = "idx_resume_analyses_user", columnList = "user_id, created_at"),
        @Index(name = "idx_resume_analyses_resume", columnList = "resume_id")
})
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @Column(name = "technical_skills_score", nullable = false)
    private int technicalSkillsScore;

    @Column(name = "projects_depth_score", nullable = false)
    private int projectsDepthScore;

    @Column(name = "experience_score", nullable = false)
    private int experienceScore;

    @Column(name = "measurable_results_score", nullable = false)
    private int measurableResultsScore;

    @Column(name = "certifications_score", nullable = false)
    private int certificationsScore;

    @Column(name = "structure_readability_score", nullable = false)
    private int structureReadabilityScore;

    @Column(name = "role_relevance_score", nullable = false)
    private int roleRelevanceScore;

    @Column(name = "strengths_json", columnDefinition = "MEDIUMTEXT")
    private String strengthsJson;

    @Column(name = "weaknesses_json", columnDefinition = "MEDIUMTEXT")
    private String weaknessesJson;

    @Column(name = "missing_information_json", columnDefinition = "MEDIUMTEXT")
    private String missingInformationJson;

    @Column(name = "actionable_recommendations_json", columnDefinition = "MEDIUMTEXT")
    private String actionableRecommendationsJson;

    @Column(name = "dimension_breakdown_json", columnDefinition = "MEDIUMTEXT")
    private String dimensionBreakdownJson;

    @Column(name = "scoring_method", nullable = false, length = 100)
    private String scoringMethod = "SCHOLARTWIN_EXPLAINABLE_RUBRIC_V1";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ResumeAnalysis() {
    }

    public ResumeAnalysis(Resume resume, User user, int overallScore, int technicalSkillsScore,
                          int projectsDepthScore, int experienceScore, int measurableResultsScore,
                          int certificationsScore, int structureReadabilityScore, int roleRelevanceScore) {
        this.resume = resume;
        this.user = user;
        this.overallScore = overallScore;
        this.technicalSkillsScore = technicalSkillsScore;
        this.projectsDepthScore = projectsDepthScore;
        this.experienceScore = experienceScore;
        this.measurableResultsScore = measurableResultsScore;
        this.certificationsScore = certificationsScore;
        this.structureReadabilityScore = structureReadabilityScore;
        this.roleRelevanceScore = roleRelevanceScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public int getTechnicalSkillsScore() {
        return technicalSkillsScore;
    }

    public void setTechnicalSkillsScore(int technicalSkillsScore) {
        this.technicalSkillsScore = technicalSkillsScore;
    }

    public int getProjectsDepthScore() {
        return projectsDepthScore;
    }

    public void setProjectsDepthScore(int projectsDepthScore) {
        this.projectsDepthScore = projectsDepthScore;
    }

    public int getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(int experienceScore) {
        this.experienceScore = experienceScore;
    }

    public int getMeasurableResultsScore() {
        return measurableResultsScore;
    }

    public void setMeasurableResultsScore(int measurableResultsScore) {
        this.measurableResultsScore = measurableResultsScore;
    }

    public int getCertificationsScore() {
        return certificationsScore;
    }

    public void setCertificationsScore(int certificationsScore) {
        this.certificationsScore = certificationsScore;
    }

    public int getStructureReadabilityScore() {
        return structureReadabilityScore;
    }

    public void setStructureReadabilityScore(int structureReadabilityScore) {
        this.structureReadabilityScore = structureReadabilityScore;
    }

    public int getRoleRelevanceScore() {
        return roleRelevanceScore;
    }

    public void setRoleRelevanceScore(int roleRelevanceScore) {
        this.roleRelevanceScore = roleRelevanceScore;
    }

    public String getStrengthsJson() {
        return strengthsJson;
    }

    public void setStrengthsJson(String strengthsJson) {
        this.strengthsJson = strengthsJson;
    }

    public String getWeaknessesJson() {
        return weaknessesJson;
    }

    public void setWeaknessesJson(String weaknessesJson) {
        this.weaknessesJson = weaknessesJson;
    }

    public String getMissingInformationJson() {
        return missingInformationJson;
    }

    public void setMissingInformationJson(String missingInformationJson) {
        this.missingInformationJson = missingInformationJson;
    }

    public String getActionableRecommendationsJson() {
        return actionableRecommendationsJson;
    }

    public void setActionableRecommendationsJson(String actionableRecommendationsJson) {
        this.actionableRecommendationsJson = actionableRecommendationsJson;
    }

    public String getDimensionBreakdownJson() {
        return dimensionBreakdownJson;
    }

    public void setDimensionBreakdownJson(String dimensionBreakdownJson) {
        this.dimensionBreakdownJson = dimensionBreakdownJson;
    }

    public String getScoringMethod() {
        return scoringMethod;
    }

    public void setScoringMethod(String scoringMethod) {
        this.scoringMethod = scoringMethod;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

