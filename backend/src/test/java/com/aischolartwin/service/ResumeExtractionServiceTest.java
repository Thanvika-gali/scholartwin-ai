package com.aischolartwin.service;

import com.aischolartwin.dto.ExtractedResumeDataDto;
import com.aischolartwin.service.extraction.DocxExtractionService;
import com.aischolartwin.service.extraction.PdfExtractionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResumeExtractionServiceTest {

    private ResumeExtractionService extractionService;

    @BeforeEach
    void setUp() {
        PdfExtractionService pdfService = Mockito.mock(PdfExtractionService.class);
        DocxExtractionService docxService = Mockito.mock(DocxExtractionService.class);
        extractionService = new ResumeExtractionService(pdfService, docxService, new ObjectMapper());
    }

    @Test
    @DisplayName("Extracts contact info, name, and skills from resume text")
    void parseStructuredData_extractsCoreDetails() {
        String sampleResumeText = """
                Alice Algorithm
                Software Engineer
                alice.algo@scholartwin.io | (555) 123-4567
                https://linkedin.com/in/alice-algo | https://github.com/alicealgo | https://alicealgo.dev
                
                SUMMARY
                Passionate software engineer experienced in distributed systems and cloud architecture.
                
                TECHNICAL SKILLS
                Languages: Java, Python, TypeScript, SQL
                Frameworks: Spring Boot, React, Node.js
                Databases & Cloud: MySQL, Redis, Docker, AWS, Git
                
                EXPERIENCE
                Google • Software Engineering Intern
                • Built high-throughput data processing pipeline reducing latency by 35%.
                • Handled 10M+ daily requests using Java and Spring Boot.
                
                PROJECTS
                ScholarTwin AI Platform
                Tech Stack: Java, Spring Boot, React, MySQL, Docker
                • Designed multi-tenant persistence architecture and real-time dashboard.
                • GitHub: https://github.com/alicealgo/scholartwin
                
                CERTIFICATIONS
                • AWS Certified Solutions Architect (2025)
                
                ACHIEVEMENTS
                • 1st Place in University ACM ICPC Regional Contest
                """;

        ExtractedResumeDataDto extracted = extractionService.parseStructuredData(sampleResumeText);

        assertEquals("alice.algo@scholartwin.io", extracted.email());
        assertEquals("Alice Algorithm", extracted.fullName());
        assertNotNull(extracted.phone());
        assertTrue(extracted.linkedinUrl().contains("linkedin.com/in/alice-algo"));
        assertTrue(extracted.githubUrl().contains("github.com/alicealgo"));
        assertTrue(extracted.portfolioUrl().contains("alicealgo.dev"));

        List<String> skills = extracted.skills();
        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Python"));
        assertTrue(skills.contains("Spring Boot"));
        assertTrue(skills.contains("React"));
        assertTrue(skills.contains("MySQL"));
        assertTrue(skills.contains("Docker"));

        assertFalse(extracted.projects().isEmpty());
        assertFalse(extracted.experience().isEmpty());
        assertFalse(extracted.certifications().isEmpty());
        assertFalse(extracted.achievements().isEmpty());
    }

    @Test
    @DisplayName("Handles sparse resume gracefully without inventing missing data")
    void parseStructuredData_handlesSparseInput() {
        String sparseText = """
                John Minimal
                john@example.com
                
                EDUCATION
                State University • B.Tech in Computer Science 2026
                """;

        ExtractedResumeDataDto extracted = extractionService.parseStructuredData(sparseText);

        assertEquals("john@example.com", extracted.email());
        assertNull(extracted.phone());
        assertNull(extracted.linkedinUrl());
        assertNull(extracted.githubUrl());
        assertTrue(extracted.skills().isEmpty());
        assertTrue(extracted.projects().isEmpty());
    }
}

