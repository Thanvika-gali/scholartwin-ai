package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "dsa_attempts",
        indexes = {
                @Index(name = "idx_dsa_attempts_problem", columnList = "problem_id, attempt_date"),
                @Index(name = "idx_dsa_attempts_user_date", columnList = "user_id, attempt_date")
        }
)
public class DSAAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private DSAProblem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attempt_date", nullable = false)
    private LocalDate attemptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 20)
    private DSAAttemptOutcome outcome = DSAAttemptOutcome.SUCCESS;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    @Column(name = "programming_language", length = 50)
    private String programmingLanguage;

    @Column(name = "learning_notes", columnDefinition = "TEXT")
    private String learningNotes;

    @Column(name = "difficulty_encountered", length = 255)
    private String difficultyEncountered;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DSAAttempt() {
    }

    public DSAAttempt(DSAProblem problem, User user, LocalDate attemptDate,
                      DSAAttemptOutcome outcome, Integer timeSpentMinutes,
                      String programmingLanguage, String learningNotes,
                      String difficultyEncountered) {
        this.problem = problem;
        this.user = user;
        this.attemptDate = attemptDate != null ? attemptDate : LocalDate.now();
        this.outcome = outcome != null ? outcome : DSAAttemptOutcome.SUCCESS;
        this.timeSpentMinutes = timeSpentMinutes;
        this.programmingLanguage = programmingLanguage;
        this.learningNotes = learningNotes;
        this.difficultyEncountered = difficultyEncountered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DSAProblem getProblem() {
        return problem;
    }

    public void setProblem(DSAProblem problem) {
        this.problem = problem;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(LocalDate attemptDate) {
        this.attemptDate = attemptDate;
    }

    public DSAAttemptOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(DSAAttemptOutcome outcome) {
        this.outcome = outcome;
    }

    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getLearningNotes() {
        return learningNotes;
    }

    public void setLearningNotes(String learningNotes) {
        this.learningNotes = learningNotes;
    }

    public String getDifficultyEncountered() {
        return difficultyEncountered;
    }

    public void setDifficultyEncountered(String difficultyEncountered) {
        this.difficultyEncountered = difficultyEncountered;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

