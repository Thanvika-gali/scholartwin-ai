package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "assessment_attempts",
        indexes = {
                @Index(name = "idx_assessment_attempts_user_assessment", columnList = "user_id, assessment_id")
        }
)
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private LearningAssessment assessment;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "score_percentage", nullable = false)
    private Integer scorePercentage;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints;

    @Column(name = "passed", nullable = false)
    private Boolean passed;

    @Column(name = "time_spent_seconds", nullable = false)
    private Integer timeSpentSeconds = 0;

    @Column(name = "user_answers_json", columnDefinition = "TEXT")
    private String userAnswersJson;

    @Column(name = "question_results_json", columnDefinition = "TEXT")
    private String questionResultsJson;

    @Column(name = "topic_breakdown_json", columnDefinition = "TEXT")
    private String topicBreakdownJson;

    @Column(name = "weak_areas_json", columnDefinition = "TEXT")
    private String weakAreasJson;

    @Column(name = "recommended_next_steps", columnDefinition = "TEXT")
    private String recommendedNextSteps;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AssessmentAttempt() {
    }

    public AssessmentAttempt(
            User user,
            LearningAssessment assessment,
            Instant startedAt,
            Instant completedAt,
            Integer scorePercentage,
            Integer totalPoints,
            Integer earnedPoints,
            Boolean passed,
            Integer timeSpentSeconds,
            String userAnswersJson,
            String questionResultsJson,
            String topicBreakdownJson,
            String weakAreasJson,
            String recommendedNextSteps
    ) {
        this.user = user;
        this.assessment = assessment;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.completedAt = completedAt != null ? completedAt : Instant.now();
        this.scorePercentage = scorePercentage;
        this.totalPoints = totalPoints;
        this.earnedPoints = earnedPoints;
        this.passed = passed;
        this.timeSpentSeconds = timeSpentSeconds != null ? timeSpentSeconds : 0;
        this.userAnswersJson = userAnswersJson;
        this.questionResultsJson = questionResultsJson;
        this.topicBreakdownJson = topicBreakdownJson;
        this.weakAreasJson = weakAreasJson;
        this.recommendedNextSteps = recommendedNextSteps;
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

    public LearningAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(LearningAssessment assessment) {
        this.assessment = assessment;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Integer scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(Integer earnedPoints) {
        this.earnedPoints = earnedPoints;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Integer getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public String getUserAnswersJson() {
        return userAnswersJson;
    }

    public void setUserAnswersJson(String userAnswersJson) {
        this.userAnswersJson = userAnswersJson;
    }

    public String getQuestionResultsJson() {
        return questionResultsJson;
    }

    public void setQuestionResultsJson(String questionResultsJson) {
        this.questionResultsJson = questionResultsJson;
    }

    public String getTopicBreakdownJson() {
        return topicBreakdownJson;
    }

    public void setTopicBreakdownJson(String topicBreakdownJson) {
        this.topicBreakdownJson = topicBreakdownJson;
    }

    public String getWeakAreasJson() {
        return weakAreasJson;
    }

    public void setWeakAreasJson(String weakAreasJson) {
        this.weakAreasJson = weakAreasJson;
    }

    public String getRecommendedNextSteps() {
        return recommendedNextSteps;
    }

    public void setRecommendedNextSteps(String recommendedNextSteps) {
        this.recommendedNextSteps = recommendedNextSteps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
