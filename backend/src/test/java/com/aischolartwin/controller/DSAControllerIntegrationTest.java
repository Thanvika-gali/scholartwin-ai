package com.aischolartwin.controller;

import com.aischolartwin.dto.DSAAttemptRequest;
import com.aischolartwin.dto.DSAProblemRequest;
import com.aischolartwin.dto.DSARevisionRequest;
import com.aischolartwin.dto.DSARevisionUpdateRequest;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.DSAAttemptRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.DSARevisionRepository;
import com.aischolartwin.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DSAControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DSAProblemRepository problemRepository;

    @Autowired
    private DSAAttemptRepository attemptRepository;

    @Autowired
    private DSARevisionRepository revisionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        userA = new User("Alice Algorithm", "alice.algo@univ.edu", passwordEncoder.encode("Secret123!"), Role.USER);
        userA = userRepository.saveAndFlush(userA);
        tokenA = jwtService.generateToken(userA);

        userB = new User("Bob Backtracker", "bob.back@univ.edu", passwordEncoder.encode("Secret123!"), Role.USER);
        userB = userRepository.saveAndFlush(userB);
        tokenB = jwtService.generateToken(userB);
    }

    @Test
    @DisplayName("Unauthenticated requests return 401 Unauthorized")
    void unauthenticatedRequests_return401() throws Exception {
        mockMvc.perform(get("/api/dsa/problems"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dsa/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dsa/topics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dsa/revisions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Complete Problem CRUD, Attempt logging, Revision scheduling, and Analytics flow")
    void completeDsaLifecycle_succeeds() throws Exception {
        // 1. Create Problem
        DSAProblemRequest createReq = new DSAProblemRequest(
                "Merge Intervals",
                "https://leetcode.com/problems/merge-intervals/",
                "LeetCode",
                DSADifficulty.MEDIUM,
                "Arrays",
                Set.of("Sorting"),
                DSAStatus.NOT_STARTED,
                "Sort by start time first",
                "Compare current start with previous end",
                "O(N log N)",
                "O(N)",
                false
        );

        MvcResult createResult = mockMvc.perform(post("/api/dsa/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Merge Intervals"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.primaryTopic").value("Arrays"))
                .andExpect(jsonPath("$.status").value("NOT_STARTED"))
                .andReturn();

        Long problemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Search / List Problems
        mockMvc.perform(get("/api/dsa/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .param("search", "Merge")
                        .param("topic", "Arrays")
                        .param("difficulty", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(problemId));

        // 3. Log a Failed Attempt (Should transition status to ATTEMPTED)
        DSAAttemptRequest failedAttempt = new DSAAttemptRequest(
                LocalDate.now(),
                DSAAttemptOutcome.FAILED,
                25,
                "Java",
                "Forgot to sort intervals before merging",
                "Sorting step missing"
        );

        mockMvc.perform(post("/api/dsa/problems/" + problemId + "/attempts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failedAttempt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("FAILED"));

        // Verify problem is now ATTEMPTED
        mockMvc.perform(get("/api/dsa/problems/" + problemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problem.status").value("ATTEMPTED"))
                .andExpect(jsonPath("$.stats.totalAttempts").value(1))
                .andExpect(jsonPath("$.stats.failedAttempts").value(1))
                .andExpect(jsonPath("$.stats.successRatePercent").value(0.0));

        // 4. Log a Successful Attempt (Should promote status to SOLVED)
        DSAAttemptRequest successAttempt = new DSAAttemptRequest(
                LocalDate.now(),
                DSAAttemptOutcome.SUCCESS,
                15,
                "Java",
                "Implemented Arrays.sort correctly with lambda",
                null
        );

        mockMvc.perform(post("/api/dsa/problems/" + problemId + "/attempts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successAttempt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.outcome").value("SUCCESS"));

        // Verify problem is now SOLVED with 50% accuracy
        mockMvc.perform(get("/api/dsa/problems/" + problemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.problem.status").value("SOLVED"))
                .andExpect(jsonPath("$.stats.totalAttempts").value(2))
                .andExpect(jsonPath("$.stats.successfulAttempts").value(1))
                .andExpect(jsonPath("$.stats.successRatePercent").value(50.0));

        // 5. Schedule a Revision
        DSARevisionRequest revisionReq = new DSARevisionRequest(
                LocalDate.now().plusDays(2),
                "Review interval edge cases (overlapping boundaries)"
        );

        MvcResult revisionResult = mockMvc.perform(post("/api/dsa/problems/" + problemId + "/revisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revisionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        Long revisionId = objectMapper.readTree(revisionResult.getResponse().getContentAsString()).get("id").asLong();

        // 6. Check Revision Queue
        mockMvc.perform(get("/api/dsa/revisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcoming", hasSize(1)))
                .andExpect(jsonPath("$.totalPendingCount").value(1));

        // 7. Complete the Revision
        DSARevisionUpdateRequest completeReq = new DSARevisionUpdateRequest(
                null, "Reviewed and tested edge cases", DSARevisionStatus.COMPLETED
        );

        mockMvc.perform(put("/api/dsa/revisions/" + revisionId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 8. Verify Dashboard Summary
        mockMvc.perform(get("/api/dsa/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProblems").value(1))
                .andExpect(jsonPath("$.solvedProblems").value(1))
                .andExpect(jsonPath("$.mediumSolved").value(1))
                .andExpect(jsonPath("$.mediumTotal").value(1))
                .andExpect(jsonPath("$.currentStreakDays").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.insights", hasSize(greaterThanOrEqualTo(1))));

        // 9. Verify Topics API
        mockMvc.perform(get("/api/dsa/topics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[?(@.topicName == 'Arrays')].totalProblems").value(hasItem(1)))
                .andExpect(jsonPath("$[?(@.topicName == 'Arrays')].solvedProblems").value(hasItem(1)))
                .andExpect(jsonPath("$[?(@.topicName == 'Arrays')].completionPercentage").value(hasItem(100.0)));

        // 10. Update Problem
        DSAProblemRequest updateReq = new DSAProblemRequest(
                "Merge Intervals Updated",
                "https://leetcode.com/problems/merge-intervals/",
                "LeetCode",
                DSADifficulty.MEDIUM,
                "Arrays",
                Set.of("Sorting"),
                DSAStatus.SOLVED,
                "Updated personal notes",
                "Optimal approach",
                "O(N log N)",
                "O(N)",
                false
        );

        mockMvc.perform(put("/api/dsa/problems/" + problemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Merge Intervals Updated"));

        // 11. Delete Problem
        mockMvc.perform(delete("/api/dsa/problems/" + problemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        // Verify problem is deleted
        mockMvc.perform(get("/api/dsa/problems/" + problemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cross-user isolation: User B cannot view, modify, or delete User A's data")
    void crossUserIsolation_enforcedStrictly() throws Exception {
        // User A creates a problem
        DSAProblem problemA = new DSAProblem(userA, "User A Secret Problem", "https://leetcode.com/problems/secret/",
                "LeetCode", DSADifficulty.HARD, "Dynamic Programming");
        problemA = problemRepository.saveAndFlush(problemA);

        // User B attempts to view User A's problem -> 404
        mockMvc.perform(get("/api/dsa/problems/" + problemA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B attempts to log attempt on User A's problem -> 404
        DSAAttemptRequest attemptReq = new DSAAttemptRequest(LocalDate.now(), DSAAttemptOutcome.SUCCESS, 20, "Java", null, null);
        mockMvc.perform(post("/api/dsa/problems/" + problemA.getId() + "/attempts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attemptReq)))
                .andExpect(status().isNotFound());

        // User B attempts to update User A's problem -> 404
        DSAProblemRequest updateReq = new DSAProblemRequest("Hacked", null, "LeetCode", DSADifficulty.EASY, "Arrays", null, null, null, null, null, null, false);
        mockMvc.perform(put("/api/dsa/problems/" + problemA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isNotFound());

        // User B attempts to delete User A's problem -> 404
        mockMvc.perform(delete("/api/dsa/problems/" + problemA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // User B listing problems sees 0
        mockMvc.perform(get("/api/dsa/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("Validation failure on invalid URL returns 400 Bad Request")
    void validationFailure_invalidUrl_returns400() throws Exception {
        DSAProblemRequest invalidReq = new DSAProblemRequest(
                "Valid Title",
                "javascript:alert(1)",
                "LeetCode",
                DSADifficulty.EASY,
                "Strings",
                null, null, null, null, null, null, false
        );

        mockMvc.perform(post("/api/dsa/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}

