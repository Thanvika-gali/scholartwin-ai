package com.aischolartwin.service;

import com.aischolartwin.dto.OrganizationSuggestionResponse;
import com.aischolartwin.dto.SuggestionActionRequest;
import com.aischolartwin.entity.*;
import com.aischolartwin.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeOrganizationServiceTest {

    @Mock
    private KnowledgeOrganizationSuggestionRepository suggestionRepository;

    @Mock
    private KnowledgeFolderRepository folderRepository;

    @Mock
    private KnowledgeRuleService ruleService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    @Mock
    private KnowledgeFolderService folderService;

    @Mock
    private KnowledgeTagRepository tagRepository;

    @Mock
    private KnowledgeResourceTagRepository resourceTagRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KnowledgeOrganizationService organizationService;

    private User user;
    private KnowledgeFolder dbmsFolder;

    @BeforeEach
    void setUp() {
        user = new User("John Academic", "john@example.com", "pass1234");
        user.setId(1L);

        dbmsFolder = new KnowledgeFolder(user, null, "DBMS", "Database Systems", "Folder", "#178582");
        dbmsFolder.setId(5L);
    }

    @Test
    void generateSuggestion_MatchedUserRule_GeneratesHighConfidenceSuggestion() {
        KnowledgeOrganizationRule rule = new KnowledgeOrganizationRule(
                user, "Auto DBMS", RuleField.FILENAME, RuleOperator.CONTAINS, "dbms",
                dbmsFolder, "sql,relational", true, 1, true
        );

        when(ruleService.evaluateRules(eq(user), eq(ResourceType.DOCUMENT), eq("dbms_lecture.pdf"), any(), any(), any()))
                .thenReturn(rule);
        when(folderService.computeFolderPath(dbmsFolder)).thenReturn("Academic Library / DBMS");
        when(documentRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(new Document()));
        when(suggestionRepository.save(any(KnowledgeOrganizationSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        KnowledgeOrganizationSuggestion suggestion = organizationService.generateSuggestion(
                user, ResourceType.DOCUMENT, 100L, "dbms_lecture.pdf", "Lecture 1", "Database normalization", "pdf",
                List.of("sql", "database"), "Computer Science (DBMS)"
        );

        assertNotNull(suggestion);
        assertEquals(dbmsFolder.getId(), suggestion.getSuggestedFolderId());
        assertTrue(suggestion.getConfidenceScore() >= 0.90);
        assertTrue(suggestion.getReasoning().contains("Matched user-defined rule"));
        assertEquals(SuggestionStatus.ACCEPTED, suggestion.getStatus()); // autoMove was true
    }

    @Test
    void generateSuggestion_DeterministicFolderNameMatch() {
        when(ruleService.evaluateRules(any(), any(), any(), any(), any(), any())).thenReturn(null);
        when(folderRepository.findByUserOrderByNameAsc(user)).thenReturn(List.of(dbmsFolder));
        when(folderService.computeFolderPath(dbmsFolder)).thenReturn("Academic Library / DBMS");
        when(suggestionRepository.save(any(KnowledgeOrganizationSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        KnowledgeOrganizationSuggestion suggestion = organizationService.generateSuggestion(
                user, ResourceType.DOCUMENT, 101L, "dbms_notes.pdf", "Intro", "relational model", "pdf",
                List.of("sql"), "Computer Science"
        );

        assertNotNull(suggestion);
        assertEquals(dbmsFolder.getId(), suggestion.getSuggestedFolderId());
        assertEquals(0.85, suggestion.getConfidenceScore());
        assertTrue(suggestion.getReasoning().contains("Matched existing folder name: 'DBMS'"));
    }

    @Test
    void acceptSuggestion_MovesResourceAndMarksAccepted() {
        KnowledgeOrganizationSuggestion suggestion = new KnowledgeOrganizationSuggestion(
                user, ResourceType.DOCUMENT, 102L, 5L, "Academic Library / DBMS",
                "Computer Science", null, "Academic Library", "[]", 0.85, "reason", false
        );
        suggestion.setId(99L);

        when(suggestionRepository.findByUserAndId(user, 99L)).thenReturn(Optional.of(suggestion));
        when(documentRepository.findByIdAndUser(102L, user)).thenReturn(Optional.of(new Document()));
        when(suggestionRepository.save(any(KnowledgeOrganizationSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

        SuggestionActionRequest request = new SuggestionActionRequest("ACCEPT", 5L, List.of("sql", "normalization"));
        OrganizationSuggestionResponse response = organizationService.acceptSuggestion(user, 99L, request);

        assertNotNull(response);
        assertEquals(SuggestionStatus.ACCEPTED, response.status());
        verify(documentRepository, times(1)).save(any(Document.class));
    }
}
