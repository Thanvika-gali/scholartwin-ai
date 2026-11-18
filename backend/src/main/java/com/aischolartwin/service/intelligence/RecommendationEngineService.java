package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.RecommendationDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.AIRecommendation;
import com.aischolartwin.entity.intelligence.RecommendationCategory;
import com.aischolartwin.entity.intelligence.RecommendationPriority;
import com.aischolartwin.entity.intelligence.RecommendationStatus;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.entity.learning.SpacedRevisionItem;
import com.aischolartwin.entity.learning.TaskStatus;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.intelligence.AIRecommendationRepository;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationEngineService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngineService.class);

    private final AIRecommendationRepository recommendationRepository;
    private final LearningProfileRepository profileRepository;
    private final LearningTaskRepository taskRepository;
    private final SpacedRevisionItemRepository revisionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public RecommendationEngineService(AIRecommendationRepository recommendationRepository,
                                       LearningProfileRepository profileRepository,
                                       LearningTaskRepository taskRepository,
                                       SpacedRevisionItemRepository revisionRepository) {
        this.recommendationRepository = recommendationRepository;
        this.profileRepository = profileRepository;
        this.taskRepository = taskRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDtos.RecommendationResponse> getRecommendations(User user, RecommendationStatus status) {
        RecommendationStatus targetStatus = status != null ? status : RecommendationStatus.PENDING;
        List<AIRecommendation> recs = recommendationRepository.findByUserAndStatusOrderByCreatedAtDesc(user, targetStatus);
        return recs.stream().map(RecommendationDtos.RecommendationResponse::fromEntity).toList();
    }

    @Transactional
    public List<RecommendationDtos.RecommendationResponse> generateRecommendations(User user) {
        List<AIRecommendation> candidates = new ArrayList<>();

        // 1. Weak concepts in profile -> revision recommendation
        profileRepository.findByUser(user).ifPresent(p -> {
            if (p.getWeakAreas() != null && !p.getWeakAreas().isBlank()) {
                String title = "Revise Core Concepts in " + p.getTargetRole();
                if (!isRecommendationAlreadyPresent(user, title)) {
                    candidates.add(new AIRecommendation(
                            user,
                            title,
                            RecommendationCategory.LEARNING_REVISION,
                            RecommendationPriority.HIGH,
                            "Your academic profile records weak areas that have not been reinforced recently.",
                            "Profile weak concepts: " + p.getWeakAreas(),
                            "Open Spaced Revision Engine and review due flashcards.",
                            "/learning?tab=spaced-revision"
                    ));
                }
            }
        });

        // 2. Pending learning tasks -> roadmap execution recommendation
        var pendingTasks = taskRepository.findByUserAndStatus(user, TaskStatus.TODO);
        if (!pendingTasks.isEmpty()) {
            var firstPending = pendingTasks.get(0);
            String title = "Complete Milestone Task: " + firstPending.getTitle();
            if (!isRecommendationAlreadyPresent(user, title)) {
                candidates.add(new AIRecommendation(
                        user,
                        title,
                        RecommendationCategory.ROADMAP_TASK,
                        RecommendationPriority.MEDIUM,
                        "Your active roadmap has pending tasks scheduled in current phase.",
                        "Estimated time: " + firstPending.getEstimatedMinutes() + " minutes",
                        "Study the task objectives and complete practical exercises.",
                        "/learning?tab=roadmaps"
                ));
            }
        }

        // 3. Algorithmic Practice Recommendation
        String dsaTitle = "Solve 2 Medium Problems on Trees & BST";
        if (!isRecommendationAlreadyPresent(user, dsaTitle)) {
            candidates.add(new AIRecommendation(
                    user,
                    dsaTitle,
                    RecommendationCategory.DSA_PRACTICE,
                    RecommendationPriority.HIGH,
                    "Consistent pattern practice increases problem-solving speed and optimal complexity recall.",
                    "DSA tracker has high-priority unsolved problem sets.",
                    "Visit DSA Tracker and log code submissions for Tree traversals.",
                    "/dsa"
            ));
        }

        // 4. Resume ATS Optimization
        String resumeTitle = "Run ATS Diagnostic Scan on Latest Resume";
        if (!isRecommendationAlreadyPresent(user, resumeTitle)) {
            candidates.add(new AIRecommendation(
                    user,
                    resumeTitle,
                    RecommendationCategory.RESUME_OPTIMIZATION,
                    RecommendationPriority.MEDIUM,
                    "Matching technical keywords against job descriptions improves interview shortlist rate.",
                    "Career Hub has new target role benchmark matrices.",
                    "Upload and re-scan your latest resume PDF in Career & Resume Hub.",
                    "/career"
            ));
        }

        if (!candidates.isEmpty()) {
            recommendationRepository.saveAll(candidates);
            log.info("Saved {} new recommendations for user: {}", candidates.size(), user.getEmail());
        }

        return recommendationRepository.findByUserAndStatusOrderByCreatedAtDesc(user, RecommendationStatus.PENDING)
                .stream()
                .map(RecommendationDtos.RecommendationResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RecommendationDtos.RecommendationResponse acceptRecommendation(User user, Long id) {
        AIRecommendation rec = recommendationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        rec.setStatus(RecommendationStatus.ACCEPTED);
        AIRecommendation saved = recommendationRepository.save(rec);
        return RecommendationDtos.RecommendationResponse.fromEntity(saved);
    }

    @Transactional
    public RecommendationDtos.RecommendationResponse dismissRecommendation(User user, Long id) {
        AIRecommendation rec = recommendationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        rec.setStatus(RecommendationStatus.DISMISSED);
        rec.setDismissedAt(Instant.now());
        AIRecommendation saved = recommendationRepository.save(rec);
        return RecommendationDtos.RecommendationResponse.fromEntity(saved);
    }

    @Transactional
    public RecommendationDtos.RecommendationResponse completeRecommendation(User user, Long id) {
        AIRecommendation rec = recommendationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        rec.setStatus(RecommendationStatus.COMPLETED);
        rec.setCompletedAt(Instant.now());
        AIRecommendation saved = recommendationRepository.save(rec);
        return RecommendationDtos.RecommendationResponse.fromEntity(saved);
    }

    private boolean isRecommendationAlreadyPresent(User user, String title) {
        return recommendationRepository.existsByUserAndTitleIgnoreCaseAndStatusIn(
                user,
                title,
                List.of(RecommendationStatus.PENDING, RecommendationStatus.ACCEPTED, RecommendationStatus.COMPLETED, RecommendationStatus.DISMISSED)
        );
    }
}
