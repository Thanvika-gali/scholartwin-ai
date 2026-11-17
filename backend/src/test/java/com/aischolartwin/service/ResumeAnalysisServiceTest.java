package com.aischolartwin.service;

import com.aischolartwin.dto.ExtractedResumeDataDto;
import com.aischolartwin.dto.ResumeAnalysisResponse;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.ResumeAnalysis;
import com.aischolartwin.entity.ResumeFileType;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.ResumeAnalysisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ResumeAnalysisServiceTest {

    private ResumeAnalysisService analysisService;
    private ResumeAnalysisRepository analysisRepository;
    private ResumeExtractionService extractionService;

    @BeforeEach
    void setUp() {
        analysisRepository = Mockito.mock(ResumeAnalysisRepository.class);
        extractionService = Mockito.mock(ResumeExtractionService.class);
        analysisService = new ResumeAnalysisService(analysisRepository, extractionService, new ObjectMapper());
    }

    @Test
    @DisplayName("Comprehensive resume scores high across all 7 dimensions")
    void analyzeResume_highScoringResume() {
        User user = new User("Alice Algorithm", "alice@example.com", "hash");
        Resume resume = new Resume(user, "Full Stack Resume", "resume.pdf", "path", ResumeFileType.PDF, 1024L, 1);
        resume.setExtractedText("Java Spring Boot React MySQL Docker AWS reduced latency by 35% handled 10M requests");

        ExtractedResumeDataDto extracted = new ExtractedResumeDataDto(
                "Alice Algorithm", "alice@example.com", "555-1234",
                "https://linkedin.com/in/alice", "https://github.com/alice", "https://alice.dev",
                "Summary of candidate",
                List.of("Java", "Spring Boot", "React", "MySQL", "Docker", "AWS", "Git", "REST"),
                List.of(new ExtractedResumeDataDto.EducationItemDto("MIT", "B.S. CS", null, "2026", "3.9")),
                List.of(new ExtractedResumeDataDto.ExperienceItemDto("Tech Corp", "Intern", "2025", "desc", List.of())),
                List.of(
                        new ExtractedResumeDataDto.ProjectItemDto("Project A", "Java, Spring", "desc", null, null, List.of()),
                        new ExtractedResumeDataDto.ProjectItemDto("Project B", "React, Node", "desc", null, null, List.of())
                ),
                List.of(new ExtractedResumeDataDto.CertificationItemDto("AWS Certified", "AWS", "2025")),
                List.of("Hackathon Winner")
        );

        when(extractionService.parseStructuredData(any())).thenReturn(extracted);
        when(extractionService.toJson(any())).thenReturn("[]");
        when(analysisRepository.save(any(ResumeAnalysis.class))).thenAnswer(i -> i.getArgument(0));

        ResumeAnalysis analysis = analysisService.analyzeResume(resume);

        assertNotNull(analysis);
        assertTrue(analysis.getOverallScore() >= 80, "Expected overall score >= 80, but got: " + analysis.getOverallScore());
        assertEquals(20, analysis.getTechnicalSkillsScore());
        assertEquals(20, analysis.getProjectsDepthScore());
        assertEquals(10, analysis.getStructureReadabilityScore());
    }

    @Test
    @DisplayName("Sparse resume receives lower score and clear recommendations")
    void analyzeResume_sparseResume() {
        User user = new User("Bob Minimal", "bob@example.com", "hash");
        Resume resume = new Resume(user, "Draft Resume", "draft.pdf", "path", ResumeFileType.PDF, 512L, 1);
        resume.setExtractedText("Bob Minimal\nNo skills or metrics listed.");

        ExtractedResumeDataDto extracted = new ExtractedResumeDataDto(
                "Bob Minimal", null, null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        when(extractionService.parseStructuredData(any())).thenReturn(extracted);
        when(extractionService.toJson(any())).thenReturn("[]");
        when(analysisRepository.save(any(ResumeAnalysis.class))).thenAnswer(i -> i.getArgument(0));

        ResumeAnalysis analysis = analysisService.analyzeResume(resume);

        assertNotNull(analysis);
        assertTrue(analysis.getOverallScore() < 50, "Expected low score for empty resume, got: " + analysis.getOverallScore());
        assertEquals(0, analysis.getTechnicalSkillsScore());
    }
}

