package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "dsa_problems",
        indexes = {
                @Index(name = "idx_dsa_problems_user_status", columnList = "user_id, status"),
                @Index(name = "idx_dsa_problems_user_topic", columnList = "user_id, primary_topic"),
                @Index(name = "idx_dsa_problems_user_difficulty", columnList = "user_id, difficulty"),
                @Index(name = "idx_dsa_problems_user_updated", columnList = "user_id, updated_at")
        }
)
public class DSAProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "problem_url", length = 500)
    private String problemUrl;

    @Column(name = "platform", nullable = false, length = 50)
    private String platform = "LEETCODE";

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DSADifficulty difficulty = DSADifficulty.MEDIUM;

    @Column(name = "primary_topic", nullable = false, length = 50)
    private String primaryTopic;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "dsa_problem_topics",
            joinColumns = @JoinColumn(name = "problem_id")
    )
    @Column(name = "topic", length = 50)
    private Set<String> additionalTopics = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DSAStatus status = DSAStatus.NOT_STARTED;

    @Column(name = "personal_notes", columnDefinition = "TEXT")
    private String personalNotes;

    @Column(name = "solution_approach", columnDefinition = "MEDIUMTEXT")
    private String solutionApproach;

    @Column(name = "time_complexity", length = 50)
    private String timeComplexity;

    @Column(name = "space_complexity", length = 50)
    private String spaceComplexity;

    @Column(name = "revision_required", nullable = false)
    private boolean revisionRequired = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "solved_at")
    private Instant solvedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DSAAttempt> attempts = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DSARevision> revisions = new ArrayList<>();

    public DSAProblem() {
    }

    public DSAProblem(User user, String title, String problemUrl, String platform,
                      DSADifficulty difficulty, String primaryTopic) {
        this.user = user;
        this.title = title;
        this.problemUrl = problemUrl;
        this.platform = (platform != null && !platform.isBlank()) ? platform.trim() : "LEETCODE";
        this.difficulty = difficulty != null ? difficulty : DSADifficulty.MEDIUM;
        this.primaryTopic = primaryTopic;
        this.status = DSAStatus.NOT_STARTED;
        this.revisionRequired = false;
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

    public String getProblemUrl() {
        return problemUrl;
    }

    public void setProblemUrl(String problemUrl) {
        this.problemUrl = problemUrl;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public DSADifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DSADifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getPrimaryTopic() {
        return primaryTopic;
    }

    public void setPrimaryTopic(String primaryTopic) {
        this.primaryTopic = primaryTopic;
    }

    public Set<String> getAdditionalTopics() {
        return additionalTopics;
    }

    public void setAdditionalTopics(Set<String> additionalTopics) {
        this.additionalTopics = additionalTopics != null ? additionalTopics : new HashSet<>();
    }

    public DSAStatus getStatus() {
        return status;
    }

    public void setStatus(DSAStatus status) {
        this.status = status;
    }

    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }

    public String getSolutionApproach() {
        return solutionApproach;
    }

    public void setSolutionApproach(String solutionApproach) {
        this.solutionApproach = solutionApproach;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getSpaceComplexity() {
        return spaceComplexity;
    }

    public void setSpaceComplexity(String spaceComplexity) {
        this.spaceComplexity = spaceComplexity;
    }

    public boolean isRevisionRequired() {
        return revisionRequired;
    }

    public void setRevisionRequired(boolean revisionRequired) {
        this.revisionRequired = revisionRequired;
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

    public Instant getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(Instant solvedAt) {
        this.solvedAt = solvedAt;
    }

    public List<DSAAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<DSAAttempt> attempts) {
        this.attempts = attempts;
    }

    public List<DSARevision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<DSARevision> revisions) {
        this.revisions = revisions;
    }
}

