package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "job_description_analyses", indexes = {
        @Index(name = "idx_job_desc_user", columnList = "user_id, created_at")
})
public class JobDescriptionAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "raw_text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String rawText;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Column(name = "experience_required", length = 100)
    private String experienceRequired;

    @Column(name = "education_required", length = 100)
    private String educationRequired;

    @Column(name = "responsibilities_json", columnDefinition = "MEDIUMTEXT")
    private String responsibilitiesJson;

    @Column(name = "match_percentage", nullable = false)
    private double matchPercentage;

    @Column(name = "matched_required_skills_json", columnDefinition = "MEDIUMTEXT")
    private String matchedRequiredSkillsJson;

    @Column(name = "missing_required_skills_json", columnDefinition = "MEDIUMTEXT")
    private String missingRequiredSkillsJson;

    @Column(name = "matched_preferred_skills_json", columnDefinition = "MEDIUMTEXT")
    private String matchedPreferredSkillsJson;

    @Column(name = "missing_preferred_skills_json", columnDefinition = "MEDIUMTEXT")
    private String missingPreferredSkillsJson;

    @Column(name = "keyword_coverage_json", columnDefinition = "MEDIUMTEXT")
    private String keywordCoverageJson;

    @Column(name = "tailored_suggestions_json", columnDefinition = "MEDIUMTEXT")
    private String tailoredSuggestionsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JobDescriptionAnalysis() {
    }

    public JobDescriptionAnalysis(User user, Resume resume, String jobTitle, String companyName,
                                  String rawText, double matchPercentage) {
        this.user = user;
        this.resume = resume;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.rawText = rawText;
        this.matchPercentage = matchPercentage;
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

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getExperienceRequired() {
        return experienceRequired;
    }

    public void setExperienceRequired(String experienceRequired) {
        this.experienceRequired = experienceRequired;
    }

    public String getEducationRequired() {
        return educationRequired;
    }

    public void setEducationRequired(String educationRequired) {
        this.educationRequired = educationRequired;
    }

    public String getResponsibilitiesJson() {
        return responsibilitiesJson;
    }

    public void setResponsibilitiesJson(String responsibilitiesJson) {
        this.responsibilitiesJson = responsibilitiesJson;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getMatchedRequiredSkillsJson() {
        return matchedRequiredSkillsJson;
    }

    public void setMatchedRequiredSkillsJson(String matchedRequiredSkillsJson) {
        this.matchedRequiredSkillsJson = matchedRequiredSkillsJson;
    }

    public String getMissingRequiredSkillsJson() {
        return missingRequiredSkillsJson;
    }

    public void setMissingRequiredSkillsJson(String missingRequiredSkillsJson) {
        this.missingRequiredSkillsJson = missingRequiredSkillsJson;
    }

    public String getMatchedPreferredSkillsJson() {
        return matchedPreferredSkillsJson;
    }

    public void setMatchedPreferredSkillsJson(String matchedPreferredSkillsJson) {
        this.matchedPreferredSkillsJson = matchedPreferredSkillsJson;
    }

    public String getMissingPreferredSkillsJson() {
        return missingPreferredSkillsJson;
    }

    public void setMissingPreferredSkillsJson(String missingPreferredSkillsJson) {
        this.missingPreferredSkillsJson = missingPreferredSkillsJson;
    }

    public String getKeywordCoverageJson() {
        return keywordCoverageJson;
    }

    public void setKeywordCoverageJson(String keywordCoverageJson) {
        this.keywordCoverageJson = keywordCoverageJson;
    }

    public String getTailoredSuggestionsJson() {
        return tailoredSuggestionsJson;
    }

    public void setTailoredSuggestionsJson(String tailoredSuggestionsJson) {
        this.tailoredSuggestionsJson = tailoredSuggestionsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

