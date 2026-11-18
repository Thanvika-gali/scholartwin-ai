package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.RecommendationDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.AIRecommendation;
import com.aischolartwin.entity.intelligence.RecommendationCategory;
import com.aischolartwin.entity.intelligence.RecommendationPriority;
import com.aischolartwin.entity.intelligence.RecommendationStatus;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.intelligence.AIRecommendationRepository;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineServiceTest {

    @Mock
    private AIRecommendationRepository recommendationRepository;

    @Mock
    private LearningProfileRepository profileRepository;

    @Mock
    private LearningTaskRepository taskRepository;

    @Mock
    private SpacedRevisionItemRepository revisionRepository;

    private RecommendationEngineService recommendationService;
    private User testUser;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationEngineService(
                recommendationRepository, profileRepository, taskRepository, revisionRepository
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("rec@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("generateRecommendations creates actionable recommendations")
    void generateRecommendations_createsActions() {
        LearningProfile profile = new LearningProfile(testUser);
        profile.setTargetRole("Systems Architect");
        profile.setWeakAreas("Raft Consensus");
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.of(profile));

        AIRecommendation rec = new AIRecommendation(testUser, "Revise Core Concepts in Systems Architect",
                RecommendationCategory.LEARNING_REVISION, RecommendationPriority.HIGH, "Reason", "Evidence", "Action", "/learning");
        rec.setId(1L);

        when(recommendationRepository.findByUserAndStatusOrderByCreatedAtDesc(testUser, RecommendationStatus.PENDING))
                .thenReturn(List.of(rec));

        List<RecommendationDtos.RecommendationResponse> list = recommendationService.generateRecommendations(testUser);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals("Revise Core Concepts in Systems Architect", list.get(0).title());
    }

    @Test
    @DisplayName("completeRecommendation sets status to COMPLETED with timestamp")
    void completeRecommendation_updatesStatus() {
        AIRecommendation rec = new AIRecommendation(testUser, "Solve 2 Medium Problems",
                RecommendationCategory.DSA_PRACTICE, RecommendationPriority.HIGH, "Reason", "Evidence", "Action", "/dsa");
        rec.setId(5L);

        when(recommendationRepository.findByIdAndUser(5L, testUser)).thenReturn(Optional.of(rec));
        when(recommendationRepository.save(any(AIRecommendation.class))).thenAnswer(inv -> inv.getArgument(0));

        RecommendationDtos.RecommendationResponse res = recommendationService.completeRecommendation(testUser, 5L);

        assertNotNull(res);
        assertEquals(RecommendationStatus.COMPLETED, res.status());
        assertNotNull(res.completedAt());
    }
}
