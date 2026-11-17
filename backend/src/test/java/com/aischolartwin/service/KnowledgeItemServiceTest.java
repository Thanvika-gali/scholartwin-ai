package com.aischolartwin.service;

import com.aischolartwin.dto.KnowledgeItemRequest;
import com.aischolartwin.dto.KnowledgeItemResponse;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeItemServiceTest {

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    @Mock
    private UserRepository userRepository;

    private KnowledgeItemService knowledgeItemService;
    private User testUser;

    @BeforeEach
    void setUp() {
        knowledgeItemService = new KnowledgeItemService(knowledgeItemRepository, userRepository);
        testUser = new User("Claude Shannon", "shannon@mit.edu", "hash", Role.USER);
        testUser.setId(10L);
    }

    @Test
    @DisplayName("Should create valid NOTE successfully")
    void shouldCreateNoteSuccessfully() {
        KnowledgeItemRequest request = new KnowledgeItemRequest(
            KnowledgeItemType.NOTE,
            "Entropy Formulation",
            "H(X) = -sum P(x) log P(x)",
            null,
            null,
            "Information Theory"
        );

        when(userRepository.findByEmailIgnoreCase("shannon@mit.edu")).thenReturn(Optional.of(testUser));
        when(knowledgeItemRepository.save(any(KnowledgeItem.class))).thenAnswer(inv -> {
            KnowledgeItem item = inv.getArgument(0);
            item.setId(100L);
            return item;
        });

        KnowledgeItemResponse response = knowledgeItemService.createItem("shannon@mit.edu", request);

        assertNotNull(response);
        assertEquals(KnowledgeItemType.NOTE, response.type());
        assertEquals("Entropy Formulation", response.title());
        assertEquals("Information Theory", response.category());
        assertEquals("H(X) = -sum P(x) log P(x)", response.content());
    }

    @Test
    @DisplayName("Should create valid LINK successfully with HTTP/HTTPS URL")
    void shouldCreateLinkSuccessfully() {
        KnowledgeItemRequest request = new KnowledgeItemRequest(
            KnowledgeItemType.LINK,
            "MIT OpenCourseWare",
            null,
            "https://ocw.mit.edu/courses/electrical-engineering-and-computer-science/",
            "EECS lecture material repository",
            "Course Resources"
        );

        when(userRepository.findByEmailIgnoreCase("shannon@mit.edu")).thenReturn(Optional.of(testUser));
        when(knowledgeItemRepository.save(any(KnowledgeItem.class))).thenAnswer(inv -> {
            KnowledgeItem item = inv.getArgument(0);
            item.setId(101L);
            return item;
        });

        KnowledgeItemResponse response = knowledgeItemService.createItem("shannon@mit.edu", request);

        assertNotNull(response);
        assertEquals(KnowledgeItemType.LINK, response.type());
        assertEquals("https://ocw.mit.edu/courses/electrical-engineering-and-computer-science/", response.url());
        assertEquals("EECS lecture material repository", response.description());
    }

    @Test
    @DisplayName("Creating NOTE with empty content throws InvalidPayloadException")
    void shouldRejectNoteWithEmptyContent() {
        KnowledgeItemRequest invalidNote = new KnowledgeItemRequest(
            KnowledgeItemType.NOTE,
            "Blank Note Title",
            "   ",
            null,
            null,
            "General"
        );

        when(userRepository.findByEmailIgnoreCase("shannon@mit.edu")).thenReturn(Optional.of(testUser));

        InvalidPayloadException ex = assertThrows(InvalidPayloadException.class, () ->
            knowledgeItemService.createItem("shannon@mit.edu", invalidNote)
        );
        assertTrue(ex.getMessage().contains("Note content cannot be empty"));
    }

    @Test
    @DisplayName("Creating LINK with empty or dangerous scheme throws InvalidPayloadException")
    void shouldRejectLinkWithDangerousScheme() {
        when(userRepository.findByEmailIgnoreCase("shannon@mit.edu")).thenReturn(Optional.of(testUser));

        // 1. Missing URL
        KnowledgeItemRequest emptyUrl = new KnowledgeItemRequest(KnowledgeItemType.LINK, "Empty Link", null, "", null, null);
        assertThrows(InvalidPayloadException.class, () -> knowledgeItemService.createItem("shannon@mit.edu", emptyUrl));

        // 2. Dangerous javascript: scheme
        KnowledgeItemRequest jsScheme = new KnowledgeItemRequest(KnowledgeItemType.LINK, "XSS Link", null, "javascript:alert(1)", null, null);
        assertThrows(InvalidPayloadException.class, () -> knowledgeItemService.createItem("shannon@mit.edu", jsScheme));

        // 3. FTP or non-HTTP scheme
        KnowledgeItemRequest ftpScheme = new KnowledgeItemRequest(KnowledgeItemType.LINK, "FTP Link", null, "ftp://files.example.com/archive", null, null);
        assertThrows(InvalidPayloadException.class, () -> knowledgeItemService.createItem("shannon@mit.edu", ftpScheme));
    }

    @Test
    @DisplayName("Accessing item belonging to another user throws ResourceNotFoundException")
    void shouldRejectCrossUserAccess() {
        when(userRepository.findByEmailIgnoreCase("shannon@mit.edu")).thenReturn(Optional.of(testUser));
        when(knowledgeItemRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            knowledgeItemService.getItemById(999L, "shannon@mit.edu")
        );
    }
}

