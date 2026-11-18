package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "learning_profiles",
        indexes = {
                @Index(name = "idx_learning_profiles_user", columnList = "user_id")
        }
)
public class LearningProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "target_role", length = 100)
    private String targetRole = "Software Development Engineer";

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_pace", nullable = false, length = 30)
    private LearningPace learningPace = LearningPace.MODERATE;

    @Column(name = "daily_study_minutes", nullable = false)
    private Integer dailyStudyMinutes = 60;

    @Column(name = "preferred_study_time", length = 50)
    private String preferredStudyTime = "MORNING";

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals;

    @Column(name = "active_topics", columnDefinition = "TEXT")
    private String activeTopics;

    @Column(name = "weak_areas", columnDefinition = "TEXT")
    private String weakAreas;

    @Column(name = "learning_interests", columnDefinition = "TEXT")
    private String learningInterests;

    @Column(name = "current_skills_json", columnDefinition = "TEXT")
    private String currentSkillsJson;

    @Column(name = "revision_intervals", length = 100)
    private String revisionIntervals = "1,3,7,14,30";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningProfile() {
    }

    public LearningProfile(User user) {
        this.user = user;
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

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public LearningPace getLearningPace() {
        return learningPace;
    }

    public void setLearningPace(LearningPace learningPace) {
        this.learningPace = learningPace;
    }

    public Integer getDailyStudyMinutes() {
        return dailyStudyMinutes;
    }

    public void setDailyStudyMinutes(Integer dailyStudyMinutes) {
        this.dailyStudyMinutes = dailyStudyMinutes;
    }

    public String getPreferredStudyTime() {
        return preferredStudyTime;
    }

    public void setPreferredStudyTime(String preferredStudyTime) {
        this.preferredStudyTime = preferredStudyTime;
    }

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }

    public String getLearningGoals() {
        return learningGoals;
    }

    public void setLearningGoals(String learningGoals) {
        this.learningGoals = learningGoals;
    }

    public String getActiveTopics() {
        return activeTopics;
    }

    public void setActiveTopics(String activeTopics) {
        this.activeTopics = activeTopics;
    }

    public String getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(String weakAreas) {
        this.weakAreas = weakAreas;
    }

    public String getLearningInterests() {
        return learningInterests;
    }

    public void setLearningInterests(String learningInterests) {
        this.learningInterests = learningInterests;
    }

    public String getCurrentSkillsJson() {
        return currentSkillsJson;
    }

    public void setCurrentSkillsJson(String currentSkillsJson) {
        this.currentSkillsJson = currentSkillsJson;
    }

    public String getRevisionIntervals() {
        return revisionIntervals;
    }

    public void setRevisionIntervals(String revisionIntervals) {
        this.revisionIntervals = revisionIntervals;
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
