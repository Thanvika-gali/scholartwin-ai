package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.InsightDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.Insight;
import com.aischolartwin.entity.intelligence.InsightCategory;
import com.aischolartwin.entity.intelligence.InsightStatus;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.intelligence.InsightRepository;
import com.aischolartwin.repository.learning.AssessmentAttemptRepository;
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
class InsightEngineServiceTest {

    @Mock
    private InsightRepository insightRepository;

    @Mock
    private LearningProfileRepository profileRepository;

    @Mock
    private LearningTaskRepository taskRepository;

    @Mock
    private SpacedRevisionItemRepository revisionRepository;

    @Mock
    private AssessmentAttemptRepository attemptRepository;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private DSAProblemRepository dsaProblemRepository;

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    private InsightEngineService insightService;
    private User testUser;

    @BeforeEach
    void setUp() {
        insightService = new InsightEngineService(
                insightRepository, profileRepository, taskRepository,
                revisionRepository, attemptRepository, careerSkillRepository,
                dsaProblemRepository, knowledgeItemRepository
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("alex@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("generateInsights creates evidence-backed insights across modules")
    void generateInsights_success() {
        LearningProfile profile = new LearningProfile(testUser);
        profile.setTargetRole("Backend Engineer");
        profile.setWeakAreas("Lock-Free Queues, B-Tree Indexing");
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.of(profile));

        when(insightRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        InsightDtos.GenerateInsightsResponse response = insightService.generateInsights(testUser, true);

        assertNotNull(response);
        assertTrue(response.insightsCount() >= 4);
        verify(insightRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("dismissInsight updates status to DISMISSED")
    void dismissInsight_success() {
        Insight insight = new Insight(testUser, "Test Insight", "Desc", InsightCategory.LEARNING, null, 0.9, null, null, null);
        insight.setId(22L);

        when(insightRepository.findByIdAndUser(22L, testUser)).thenReturn(Optional.of(insight));
        when(insightRepository.save(any(Insight.class))).thenAnswer(inv -> inv.getArgument(0));

        InsightDtos.InsightResponse res = insightService.dismissInsight(testUser, 22L);

        assertNotNull(res);
        assertEquals(InsightStatus.DISMISSED, res.status());
    }
}
