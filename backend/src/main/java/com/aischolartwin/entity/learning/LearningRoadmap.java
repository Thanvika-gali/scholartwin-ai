package com.aischolartwin.entity.learning;

import com.aischolartwin.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "learning_roadmaps",
        indexes = {
                @Index(name = "idx_roadmaps_user_status", columnList = "user_id, status")
        }
)
public class LearningRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Column(name = "goal_type", length = 50)
    private String goalType = "ROLE"; // ROLE, SKILL, TOPIC, INTERVIEW, CUSTOM

    @Column(name = "target_skill_or_topic", length = 150)
    private String targetSkillOrTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RoadmapStatus status = RoadmapStatus.NOT_STARTED;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "estimated_hours")
    private Double estimatedHours = 0.0;

    @Column(name = "completed_hours")
    private Double completedHours = 0.0;

    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("phaseOrder ASC")
    private List<RoadmapPhase> phases = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LearningRoadmap() {
    }

    public LearningRoadmap(User user, String title, String description, String targetRole, String goalType, String targetSkillOrTopic) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.targetRole = targetRole;
        this.goalType = goalType != null ? goalType : "ROLE";
        this.targetSkillOrTopic = targetSkillOrTopic;
        this.status = RoadmapStatus.IN_PROGRESS;
        this.completionPercentage = 0;
    }

    public void addPhase(RoadmapPhase phase) {
        phases.add(phase);
        phase.setRoadmap(this);
    }

    public void removePhase(RoadmapPhase phase) {
        phases.remove(phase);
        phase.setRoadmap(null);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getTargetSkillOrTopic() {
        return targetSkillOrTopic;
    }

    public void setTargetSkillOrTopic(String targetSkillOrTopic) {
        this.targetSkillOrTopic = targetSkillOrTopic;
    }

    public RoadmapStatus getStatus() {
        return status;
    }

    public void setStatus(RoadmapStatus status) {
        this.status = status;
    }

    public LocalDate getTargetCompletionDate() {
        return targetCompletionDate;
    }

    public void setTargetCompletionDate(LocalDate targetCompletionDate) {
        this.targetCompletionDate = targetCompletionDate;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Double getCompletedHours() {
        return completedHours;
    }

    public void setCompletedHours(Double completedHours) {
        this.completedHours = completedHours;
    }

    public Integer getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Integer completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public List<RoadmapPhase> getPhases() {
        return phases;
    }

    public void setPhases(List<RoadmapPhase> phases) {
        this.phases.clear();
        if (phases != null) {
            this.phases.addAll(phases);
        }
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
