package com.aischolartwin.controller;

import com.aischolartwin.dto.KnowledgeItemRequest;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KnowledgeItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeItemRepository knowledgeItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private User userB;
    private String tokenUserA;
    private String tokenUserB;

    @BeforeEach
    void setUp() {
        userA = new User("Alice Turing", "alice@test.ac.uk", passwordEncoder.encode("PassAlice123!"), Role.USER);
        userA = userRepository.saveAndFlush(userA);
        tokenUserA = jwtService.generateToken(userA);

        userB = new User("Bob Lovelace", "bob@test.ac.uk", passwordEncoder.encode("PassBob123!"), Role.USER);
        userB = userRepository.saveAndFlush(userB);
        tokenUserB = jwtService.generateToken(userB);
    }

    @Test
    @DisplayName("GET /api/knowledge-items without authentication returns 401")
    void getItemsWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/knowledge-items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/knowledge-items creates Note and returns 201")
    void createNoteShouldReturn201() throws Exception {
        KnowledgeItemRequest request = new KnowledgeItemRequest(
            KnowledgeItemType.NOTE,
            "Graph Traversal BFS",
            "Breadth-First Search uses a Queue and visits neighbor vertices level by level.",
            null,
            null,
            "Algorithms"
        );

        mockMvc.perform(post("/api/knowledge-items")
                .header("Authorization", "Bearer " + tokenUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("NOTE"))
                .andExpect(jsonPath("$.title").value("Graph Traversal BFS"))
                .andExpect(jsonPath("$.category").value("Algorithms"));
    }

    @Test
    @DisplayName("POST /api/knowledge-items creates Link and returns 201")
    void createLinkShouldReturn201() throws Exception {
        KnowledgeItemRequest request = new KnowledgeItemRequest(
            KnowledgeItemType.LINK,
            "Visualgo Data Structures",
            null,
            "https://visualgo.net/en",
            "Interactive animation for DSA algorithms",
            "Tools"
        );

        mockMvc.perform(post("/api/knowledge-items")
                .header("Authorization", "Bearer " + tokenUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("LINK"))
                .andExpect(jsonPath("$.url").value("https://visualgo.net/en"));
    }

    @Test
    @DisplayName("Cross-user isolation: User B cannot access or modify User A's item")
    void crossUserAccessShouldReturn404() throws Exception {
        KnowledgeItem itemA = new KnowledgeItem(
            userA,
            KnowledgeItemType.NOTE,
            "Confidential Research Note",
            "Private notes belonging to User A",
            null,
            null,
            "Research"
        );
        itemA = knowledgeItemRepository.saveAndFlush(itemA);

        // User B attempts to GET User A's item
        mockMvc.perform(get("/api/knowledge-items/" + itemA.getId())
                .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isNotFound());

        // User B attempts to DELETE User A's item
        mockMvc.perform(delete("/api/knowledge-items/" + itemA.getId())
                .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isNotFound());

        // User B attempts to PUT User A's item
        KnowledgeItemRequest modifyReq = new KnowledgeItemRequest(
            KnowledgeItemType.NOTE, "Hacked Note", "Attacking content", null, null, "Research"
        );
        mockMvc.perform(put("/api/knowledge-items/" + itemA.getId())
                .header("Authorization", "Bearer " + tokenUserB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Filter and search: returns only matching items for authenticated user")
    void searchAndFilterShouldReturnMatches() throws Exception {
        KnowledgeItem note = new KnowledgeItem(userA, KnowledgeItemType.NOTE, "Red-Black Trees", "Self balancing BST", null, null, "Data Structures");
        KnowledgeItem link = new KnowledgeItem(userA, KnowledgeItemType.LINK, "LeetCode Practice", null, "https://leetcode.com", "Practice problems", "Algorithms");
        knowledgeItemRepository.saveAndFlush(note);
        knowledgeItemRepository.saveAndFlush(link);

        // Filter by NOTE
        mockMvc.perform(get("/api/knowledge-items?type=NOTE")
                .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Red-Black Trees"));

        // Search by keyword "LeetCode"
        mockMvc.perform(get("/api/knowledge-items?search=LeetCode")
                .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("LeetCode Practice"));
    }
}

