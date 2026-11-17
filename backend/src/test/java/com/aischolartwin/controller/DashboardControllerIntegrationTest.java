package com.aischolartwin.controller;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentFormat;
import com.aischolartwin.entity.DocumentStatus;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeItemRepository knowledgeItemRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User primaryUser;
    private User otherUser;
    private String primaryToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        primaryUser = new User("Primary Scholar", "primary@scholar.edu", passwordEncoder.encode("Pass123!"), Role.USER);
        primaryUser = userRepository.saveAndFlush(primaryUser);
        primaryToken = jwtService.generateToken(primaryUser);

        otherUser = new User("Other Scholar", "other@scholar.edu", passwordEncoder.encode("Pass123!"), Role.USER);
        otherUser = userRepository.saveAndFlush(otherUser);
        otherToken = jwtService.generateToken(otherUser);
    }

    @Test
    @DisplayName("GET /api/dashboard/summary without auth returns 401")
    void summaryWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/dashboard/summary returns zero counts for new user")
    void summaryForNewUserShouldReturnZero() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.totalNotes").value(0))
                .andExpect(jsonPath("$.totalLinks").value(0))
                .andExpect(jsonPath("$.totalDocuments").value(0))
                .andExpect(jsonPath("$.recentItems", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/dashboard/summary calculates accurate counts and excludes other users' data")
    void summaryShouldCalculateAccurately() throws Exception {
        // Add items for primary user: 2 notes, 1 link
        knowledgeItemRepository.saveAndFlush(new KnowledgeItem(primaryUser, KnowledgeItemType.NOTE, "Note 1", "Content 1", null, null, "OS"));
        knowledgeItemRepository.saveAndFlush(new KnowledgeItem(primaryUser, KnowledgeItemType.NOTE, "Note 2", "Content 2", null, null, "DSA"));
        knowledgeItemRepository.saveAndFlush(new KnowledgeItem(primaryUser, KnowledgeItemType.LINK, "Link 1", null, "https://docs.oracle.com", "Java Docs", "Java"));

        // Add 1 document for primary user
        Document doc = new Document(primaryUser, "paper.pdf", "uploads/documents/fake.pdf", "pdf",
                "application/pdf", 1024L, DocumentFormat.PDF);
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setPageCount(1);
        doc.setCharacterCount(100);
        doc.setChunkCount(2);
        documentRepository.saveAndFlush(doc);

        // Add 5 items for other user (must not affect primary user)
        knowledgeItemRepository.saveAndFlush(new KnowledgeItem(otherUser, KnowledgeItemType.NOTE, "Other Note", "Other Content", null, null, "General"));

        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(4))
                .andExpect(jsonPath("$.totalNotes").value(2))
                .andExpect(jsonPath("$.totalLinks").value(1))
                .andExpect(jsonPath("$.totalDocuments").value(1))
                .andExpect(jsonPath("$.recentItems", hasSize(3)));
    }
}

