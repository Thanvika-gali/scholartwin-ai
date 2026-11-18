package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "learning_assessments",
        indexes = {
                @Index(name = "idx_assessments_user_topic", columnList = "user_id, topic")
        }
)
public class LearningAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 30)
    private AssessmentType assessmentType = AssessmentType.TOPIC_QUIZ;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 30)
    private AssessmentDifficulty difficulty = AssessmentDifficulty.INTERMEDIATE;

    @Column(name = "passing_score_percentage", nullable = false)
    private Integer passingScorePercentage = 70;

    @Column(name = "time_limit_minutes", nullable = false)
    private Integer timeLimitMinutes = 20;

    @Column(name = "source", length = 50)
    private String source = "CURATED"; // AI_GENERATED, CURATED, CUSTOM

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    private List<AssessmentQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningAssessment() {
    }

    public LearningAssessment(
            User user,
            String title,
            String topic,
            String description,
            AssessmentType assessmentType,
            AssessmentDifficulty difficulty,
            Integer passingScorePercentage,
            Integer timeLimitMinutes,
            String source
    ) {
        this.user = user;
        this.title = title;
        this.topic = topic;
        this.description = description;
        this.assessmentType = assessmentType != null ? assessmentType : AssessmentType.TOPIC_QUIZ;
        this.difficulty = difficulty != null ? difficulty : AssessmentDifficulty.INTERMEDIATE;
        this.passingScorePercentage = passingScorePercentage != null ? passingScorePercentage : 70;
        this.timeLimitMinutes = timeLimitMinutes != null ? timeLimitMinutes : 20;
        this.source = source != null ? source : "CURATED";
    }

    public void addQuestion(AssessmentQuestion question) {
        questions.add(question);
        question.setAssessment(this);
    }

    public void removeQuestion(AssessmentQuestion question) {
        questions.remove(question);
        question.setAssessment(null);
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public AssessmentDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(AssessmentDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getPassingScorePercentage() {
        return passingScorePercentage;
    }

    public void setPassingScorePercentage(Integer passingScorePercentage) {
        this.passingScorePercentage = passingScorePercentage;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<AssessmentQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AssessmentQuestion> questions) {
        this.questions = questions;
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
