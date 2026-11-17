package com.aischolartwin.controller;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.*;
import com.aischolartwin.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CareerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private CareerSkillRepository careerSkillRepository;

    @Autowired
    private CareerActionRepository careerActionRepository;

    @Autowired
    private JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        careerActionRepository.deleteAll();
        careerSkillRepository.deleteAll();
        jobDescriptionAnalysisRepository.deleteAll();
        resumeRepository.deleteAll();

        String emailA = "alice.career." + System.currentTimeMillis() + "@univ.edu";
        userA = new User("Alice Career", emailA, passwordEncoder.encode("pass123"));
        userA = userRepository.save(userA);
        tokenA = jwtService.generateToken(userA);

        String emailB = "bob.career." + System.currentTimeMillis() + "@univ.edu";
        userB = new User("Bob Career", emailB, passwordEncoder.encode("pass123"));
        userB = userRepository.save(userB);
        tokenB = jwtService.generateToken(userB);
    }

    @Test
    @DisplayName("Complete Career Intelligence Lifecycle: Upload, Parse, Analyze, Skills, Roles, Actions, and Dashboard")
    void completeCareerLifecycle_succeeds() throws Exception {
        // 1. Upload PDF Resume with valid magic bytes
        byte[] pdfBytes = "%PDF-1.4\nAlice Algorithm\nSoftware Engineer\nalice@example.com\nJava, Spring Boot, React, MySQL, Docker\nProjects: Built Cloud Pipeline\n".getBytes();
        MockMultipartFile resumeFile = new MockMultipartFile(
                "file", "alice_resume.pdf", "application/pdf", pdfBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/career/resumes")
                        .file(resumeFile)
                        .param("title", "Alice Main Resume 2026")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resume.id").isNumber())
                .andExpect(jsonPath("$.resume.title").value("Alice Main Resume 2026"))
                .andExpect(jsonPath("$.resume.isPrimary").value(true))
                .andExpect(jsonPath("$.resume.fileType").value("PDF"))
                .andReturn();

        Long resumeId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .get("resume").get("id").asLong();

        // 2. Get Resumes List
        mockMvc.perform(get("/api/career/resumes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(resumeId));

        // 3. Get Resume Details
        mockMvc.perform(get("/api/career/resumes/" + resumeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resume.id").value(resumeId));

        // 4. Skills Inventory CRUD
        CareerSkillRequest skillReq = new CareerSkillRequest(
                "Kubernetes", CareerSkillCategory.CLOUD_DEVOPS, SkillProficiency.ADVANCED,
                SkillSource.USER_ENTERED, "Container orchestration production experience", true
        );

        MvcResult skillResult = mockMvc.perform(post("/api/career/skills")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skillName").value("Kubernetes"))
                .andExpect(jsonPath("$.category").value("CLOUD_DEVOPS"))
                .andReturn();

        Long skillId = objectMapper.readTree(skillResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/career/skills")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 5. Role Readiness
        mockMvc.perform(get("/api/career/roles/sde/readiness")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value("sde"))
                .andExpect(jsonPath("$.readinessPercentage").isNumber());

        // 6. Job Description Analysis
        JobDescriptionAnalysisRequest jdReq = new JobDescriptionAnalysisRequest(
                "Full Stack Engineer", "Stripe",
                "Requirements: Java, Spring Boot, React, SQL. Preferred: Docker, Kubernetes.",
                resumeId
        );

        mockMvc.perform(post("/api/career/job-descriptions/analyze")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jdReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Full Stack Engineer"))
                .andExpect(jsonPath("$.matchPercentage").isNumber());

        // 7. Career Action CRUD
        CareerActionRequest actionReq = new CareerActionRequest(
                "Practice Dynamic Programming Patterns",
                "Solve 5 DP problems on LeetCode before technical screening",
                "Role gap analysis identified advanced algorithmic questions",
                CareerActionPriority.HIGH,
                CareerActionCategory.DSA_PRACTICE,
                "SDE",
                LocalDate.now().plusDays(5)
        );

        MvcResult actionResult = mockMvc.perform(post("/api/career/actions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Practice Dynamic Programming Patterns"))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andReturn();

        Long actionId = objectMapper.readTree(actionResult.getResponse().getContentAsString()).get("id").asLong();

        // Mark action completed
        CareerActionUpdateRequest updateActionReq = new CareerActionUpdateRequest(
                null, null, null, null, null, null, CareerActionStatus.COMPLETED, null
        );

        mockMvc.perform(put("/api/career/actions/" + actionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateActionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 8. Career Dashboard Summary
        mockMvc.perform(get("/api/career/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResumes").value(1))
                .andExpect(jsonPath("$.totalSkillsCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.completedActionsCount").value(1))
                .andExpect(jsonPath("$.careerInsights", hasSize(greaterThanOrEqualTo(1))));

        // 9. Delete Resume
        mockMvc.perform(delete("/api/career/resumes/" + resumeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/career/resumes/" + resumeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cross-User Isolation: User B cannot view, modify, or delete User A's career data")
    void crossUserIsolation_enforced() throws Exception {
        // User A creates a skill
        CareerSkill skillA = new CareerSkill(userA, "Rust", CareerSkillCategory.PROGRAMMING_LANGUAGES,
                SkillProficiency.INTERMEDIATE, SkillSource.USER_ENTERED, null);
        skillA = careerSkillRepository.save(skillA);

        // User A creates a career action
        CareerAction actionA = new CareerAction(userA, "Private Goal", "Desc", "Reason",
                CareerActionPriority.HIGH, CareerActionCategory.GENERAL, "Secret", LocalDate.now().plusDays(3));
        actionA = careerActionRepository.save(actionA);

        // User B attempts to delete User A's skill -> 404
        mockMvc.perform(delete("/api/career/skills/" + skillA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B attempts to update User A's action -> 404
        CareerActionUpdateRequest updateReq = new CareerActionUpdateRequest(
                "Tampered Title", null, null, null, null, null, CareerActionStatus.COMPLETED, null
        );
        mockMvc.perform(put("/api/career/actions/" + actionA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());
    }
}

