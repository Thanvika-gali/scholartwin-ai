package com.aischolartwin.service;

import com.aischolartwin.dto.JobDescriptionAnalysisRequest;
import com.aischolartwin.dto.JobDescriptionAnalysisResponse;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.JobDescriptionAnalysis;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.JobDescriptionAnalysisRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JobDescriptionServiceTest {

    private JobDescriptionService jobDescriptionService;
    private JobDescriptionAnalysisRepository jobRepo;
    private ResumeRepository resumeRepo;
    private CareerSkillRepository skillRepo;
    private ResumeExtractionService extractionService;

    @BeforeEach
    void setUp() {
        jobRepo = Mockito.mock(JobDescriptionAnalysisRepository.class);
        resumeRepo = Mockito.mock(ResumeRepository.class);
        skillRepo = Mockito.mock(CareerSkillRepository.class);
        extractionService = Mockito.mock(ResumeExtractionService.class);
        jobDescriptionService = new JobDescriptionService(jobRepo, resumeRepo, skillRepo, extractionService, new ObjectMapper());
    }

    @Test
    @DisplayName("Matches job description against verified skills and calculates match percentage")
    void analyzeJobDescription_calculatesMatch() {
        User user = new User("Alice", "alice@test.com", "hash");

        List<CareerSkill> skills = List.of(
                new CareerSkill(user, "Java", CareerSkillCategory.PROGRAMMING_LANGUAGES, null, null, null),
                new CareerSkill(user, "Spring Boot", CareerSkillCategory.BACKEND, null, null, null),
                new CareerSkill(user, "MySQL", CareerSkillCategory.DATABASES, null, null, null)
        );
        when(skillRepo.findByUserOrderByCategoryAscSkillNameAsc(user)).thenReturn(skills);

        String jdText = """
                Software Engineer - Backend
                Acme Corp
                Requirements:
                - Strong proficiency in Java and Spring Boot
                - Experience with MySQL and Docker
                Preferred:
                - Experience with AWS and Kubernetes
                """;

        when(extractionService.extractSkills(any())).thenReturn(List.of("Java", "Spring Boot", "MySQL", "Docker", "AWS", "Kubernetes"));
        when(extractionService.toJson(any())).thenReturn("[]");
        when(jobRepo.save(any(JobDescriptionAnalysis.class))).thenAnswer(i -> {
            JobDescriptionAnalysis entity = i.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        JobDescriptionAnalysisRequest req = new JobDescriptionAnalysisRequest(
                "Software Engineer - Backend", "Acme Corp", jdText, null
        );

        JobDescriptionAnalysisResponse response = jobDescriptionService.analyzeJobDescription(user, req);

        assertNotNull(response);
        assertEquals("Software Engineer - Backend", response.jobTitle());
        assertTrue(response.matchedRequiredSkills().contains("Java"));
        assertTrue(response.matchedRequiredSkills().contains("Spring Boot"));
        assertTrue(response.missingRequiredSkills().contains("Docker"));
        assertTrue(response.matchPercentage() > 40.0);
        assertFalse(response.tailoredSuggestions().isEmpty());
    }
}

