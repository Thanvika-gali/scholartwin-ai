package com.aischolartwin.service;

import com.aischolartwin.dto.RoleReadinessDto;
import com.aischolartwin.dto.SkillGapAnalysisResponse;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RoleReadinessServiceTest {

    private RoleReadinessService roleReadinessService;
    private CareerSkillRepository careerSkillRepository;
    private DSAProblemRepository dsaProblemRepository;
    private ResumeRepository resumeRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        careerSkillRepository = Mockito.mock(CareerSkillRepository.class);
        dsaProblemRepository = Mockito.mock(DSAProblemRepository.class);
        resumeRepository = Mockito.mock(ResumeRepository.class);
        objectMapper = new ObjectMapper();
        when(resumeRepository.findByUserAndIsPrimaryTrue(any())).thenReturn(Optional.empty());
        when(resumeRepository.findByUserOrderByCreatedAtDesc(any())).thenReturn(List.of());

        roleReadinessService = new RoleReadinessService(careerSkillRepository, dsaProblemRepository, resumeRepository, objectMapper);
    }

    @Test
    @DisplayName("Calculates SDE role readiness and includes DSA problem evidence")
    void evaluateRoleReadiness_sdeRole() {
        User user = new User("Alice", "alice@test.com", "hash");

        List<CareerSkill> userSkills = List.of(
                new CareerSkill(user, "Java", CareerSkillCategory.PROGRAMMING_LANGUAGES, null, null, "Verified in resume"),
                new CareerSkill(user, "Data Structures", CareerSkillCategory.CORE_CS, null, null, null),
                new CareerSkill(user, "Algorithms", CareerSkillCategory.CORE_CS, null, null, null),
                new CareerSkill(user, "SQL", CareerSkillCategory.DATABASES, null, null, null),
                new CareerSkill(user, "Git", CareerSkillCategory.TOOLS, null, null, null)
        );

        when(careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user)).thenReturn(userSkills);
        when(dsaProblemRepository.countByUserAndStatus(eq(user), eq(DSAStatus.SOLVED))).thenReturn(15L);

        RoleReadinessDto readiness = roleReadinessService.evaluateRoleReadiness(user, "sde");

        assertNotNull(readiness);
        assertEquals("sde", readiness.roleId());
        assertTrue(readiness.matchedRequiredSkills().contains("Java"));
        assertTrue(readiness.matchedRequiredSkills().contains("SQL"));
        assertTrue(readiness.readinessPercentage() > 50.0);
        assertNotNull(readiness.dsaSupportingEvidence());
        assertTrue(readiness.dsaSupportingEvidence().contains("15 solved DSA coding problems"));
    }

    @Test
    @DisplayName("Evaluates all 8 benchmark roles in SkillGapAnalysisResponse")
    void getSkillGapAnalysis_evaluatesAllEightRoles() {
        User user = new User("Bob", "bob@test.com", "hash");

        when(careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user)).thenReturn(List.of());
        when(careerSkillRepository.countByUser(user)).thenReturn(0L);

        SkillGapAnalysisResponse gapAnalysis = roleReadinessService.getSkillGapAnalysis(user, "java-developer");

        assertEquals(8, gapAnalysis.allRoles().size());
        assertEquals("java-developer", gapAnalysis.targetRoleReadiness().roleId());
        assertFalse(gapAnalysis.topMissingSkillsAcrossRoles().isEmpty());
    }
}

