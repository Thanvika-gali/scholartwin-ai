package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes", indexes = {
        @Index(name = "idx_resumes_user_primary", columnList = "user_id, is_primary"),
        @Index(name = "idx_resumes_user_version", columnList = "user_id, version_number"),
        @Index(name = "idx_resumes_user_status", columnList = "user_id, processing_status"),
        @Index(name = "idx_resumes_user_created", columnList = "user_id, created_at")
})
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private ResumeFileType fileType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "version_number", nullable = false)
    private int versionNumber = 1;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 30)
    private ResumeProcessingStatus processingStatus = ResumeProcessingStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "education_json", columnDefinition = "MEDIUMTEXT")
    private String educationJson;

    @Column(name = "skills_json", columnDefinition = "MEDIUMTEXT")
    private String skillsJson;

    @Column(name = "experience_json", columnDefinition = "MEDIUMTEXT")
    private String experienceJson;

    @Column(name = "projects_json", columnDefinition = "MEDIUMTEXT")
    private String projectsJson;

    @Column(name = "certifications_json", columnDefinition = "MEDIUMTEXT")
    private String certificationsJson;

    @Column(name = "achievements_json", columnDefinition = "MEDIUMTEXT")
    private String achievementsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeAnalysis> analyses = new ArrayList<>();

    public Resume() {
    }

    public Resume(User user, String title, String originalFilename, String storagePath,
                  ResumeFileType fileType, Long fileSizeBytes, int versionNumber) {
        this.user = user;
        this.title = title;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
        this.versionNumber = versionNumber;
        this.processingStatus = ResumeProcessingStatus.PENDING;
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

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public ResumeFileType getFileType() {
        return fileType;
    }

    public void setFileType(ResumeFileType fileType) {
        this.fileType = fileType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public ResumeProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ResumeProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getEducationJson() {
        return educationJson;
    }

    public void setEducationJson(String educationJson) {
        this.educationJson = educationJson;
    }

    public String getSkillsJson() {
        return skillsJson;
    }

    public void setSkillsJson(String skillsJson) {
        this.skillsJson = skillsJson;
    }

    public String getExperienceJson() {
        return experienceJson;
    }

    public void setExperienceJson(String experienceJson) {
        this.experienceJson = experienceJson;
    }

    public String getProjectsJson() {
        return projectsJson;
    }

    public void setProjectsJson(String projectsJson) {
        this.projectsJson = projectsJson;
    }

    public String getCertificationsJson() {
        return certificationsJson;
    }

    public void setCertificationsJson(String certificationsJson) {
        this.certificationsJson = certificationsJson;
    }

    public String getAchievementsJson() {
        return achievementsJson;
    }

    public void setAchievementsJson(String achievementsJson) {
        this.achievementsJson = achievementsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ResumeAnalysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<ResumeAnalysis> analyses) {
        this.analyses = analyses;
    }
}

