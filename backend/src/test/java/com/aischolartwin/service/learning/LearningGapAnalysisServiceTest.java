package com.aischolartwin.service.learning;

import com.aischolartwin.dto.RoleReadinessDto;
import com.aischolartwin.dto.learning.LearningGapReportDto;
import com.aischolartwin.entity.*;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.learning.AssessmentAttemptRepository;
import com.aischolartwin.repository.learning.SpacedRevisionItemRepository;
import com.aischolartwin.service.RoleReadinessService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningGapAnalysisServiceTest {

    @Mock
    private LearningProfileService profileService;

    @Mock
    private RoleReadinessService roleReadinessService;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private DSAProblemRepository dsaProblemRepository;

    @Mock
    private DSARevisionRepository dsaRevisionRepository;

    @Mock
    private SpacedRevisionItemRepository spacedRevisionItemRepository;

    @Mock
    private AssessmentAttemptRepository assessmentAttemptRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    private LearningGapAnalysisService gapAnalysisService;
    private User testUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        gapAnalysisService = new LearningGapAnalysisService(
                profileService,
                roleReadinessService,
                careerSkillRepository,
                dsaProblemRepository,
                dsaRevisionRepository,
                spacedRevisionItemRepository,
                assessmentAttemptRepository,
                documentRepository,
                knowledgeItemRepository,
                objectMapper
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Grace Engineer");
        testUser.setEmail("grace@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("analyzeGaps synthesizes career skills, role benchmarks, and generates explainable recommendations")
    void analyzeGaps_synthesizesSourcesAndGeneratesRecommendations() {
        LearningProfile profile = new LearningProfile(testUser);
        profile.setTargetRole("Software Development Engineer");
        profile.setCurrentSkillsJson("[\"Java\", \"Spring Boot\"]");
        profile.setWeakAreas("[\"Dynamic Programming\", \"Distributed Systems\"]");

        when(profileService.getOrCreateProfileEntity(testUser)).thenReturn(profile);

        RoleReadinessDto roleReadiness = new RoleReadinessDto(
                "sde",
                "Software Development Engineer",
                75.0,
                3, 4, 2, 3,
                List.of("Java", "OOP"),
                List.of("Kubernetes"),
                List.of("Docker"),
                List.of("Kafka"),
                List.of("Kubernetes", "Kafka"),
                List.of("Coursework in Java"),
                "Solved 10 array questions",
                List.of("Java", "MySQL"),
                List.of("Java"),
                List.of("SQL"),
                List.of("Kubernetes")
        );
        when(roleReadinessService.evaluateRoleReadiness(eq(testUser), anyString())).thenReturn(roleReadiness);

        CareerSkill skill1 = new CareerSkill(testUser, "Java", CareerSkillCategory.PROGRAMMING_LANGUAGES, SkillProficiency.EXPERT, SkillSource.USER_ENTERED, "Built multi-tier enterprise systems");
        CareerSkill skill2 = new CareerSkill(testUser, "Kubernetes", CareerSkillCategory.CLOUD_DEVOPS, SkillProficiency.BEGINNER, SkillSource.USER_ENTERED, "Introductory container orchestration");
        when(careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(testUser)).thenReturn(List.of(skill1, skill2));

        DSAProblem problem1 = new DSAProblem(testUser, "Two Sum", "https://leetcode.com/problems/two-sum", "LeetCode", DSADifficulty.EASY, "Array");
        problem1.setStatus(DSAStatus.SOLVED);
        when(dsaProblemRepository.findByUserOrderByUpdatedAtDesc(testUser)).thenReturn(List.of(problem1));

        when(dsaRevisionRepository.findByUserAndStatusOrderByScheduledDateAsc(eq(testUser), any())).thenReturn(Collections.emptyList());
        when(spacedRevisionItemRepository.findByUserOrderByScheduledDateAsc(testUser)).thenReturn(Collections.emptyList());
        when(assessmentAttemptRepository.findByUserOrderByCompletedAtDesc(testUser)).thenReturn(Collections.emptyList());
        when(documentRepository.countByUser(testUser)).thenReturn(5L);
        when(knowledgeItemRepository.countByUser(testUser)).thenReturn(10L);

        LearningGapReportDto.Response report = gapAnalysisService.analyzeGaps(testUser);

        assertNotNull(report);
        assertEquals("Software Development Engineer", report.targetRole());
        assertEquals(75, report.overallReadinessPercentage());
        assertFalse(report.strongTopics().isEmpty());
        assertTrue(report.strongTopics().stream().anyMatch(s -> s.topic().equalsIgnoreCase("Java")));
        assertFalse(report.prioritizedRecommendations().isEmpty());
        assertNotNull(report.topicMasteryDistribution());
    }
}
