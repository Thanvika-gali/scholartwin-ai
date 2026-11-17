package com.aischolartwin.service;

import com.aischolartwin.dto.FolderDeleteRequest;
import com.aischolartwin.dto.FolderMoveRequest;
import com.aischolartwin.dto.FolderRequest;
import com.aischolartwin.dto.FolderResponse;
import com.aischolartwin.dto.FolderTreeResponse;
import com.aischolartwin.entity.KnowledgeFolder;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeFolderRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
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
class KnowledgeFolderServiceTest {

    @Mock
    private KnowledgeFolderRepository folderRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    @InjectMocks
    private KnowledgeFolderService folderService;

    private User user;
    private KnowledgeFolder rootFolder;
    private KnowledgeFolder childFolder;

    @BeforeEach
    void setUp() {
        user = new User("Jane Doe", "jane@example.com", "secret123");
        user.setId(1L);

        rootFolder = new KnowledgeFolder(user, null, "Computer Science", "CS Core Notes", "Folder", "#178582");
        rootFolder.setId(10L);

        childFolder = new KnowledgeFolder(user, rootFolder, "Database Systems", "DBMS course notes", "Database", "#BFA181");
        childFolder.setId(20L);
    }

    @Test
    void createFolder_Root_SavesSuccessfully() {
        FolderRequest request = new FolderRequest("Operating Systems", null, "OS Topics", "Cpu", "#0A1828", false, false);

        when(folderRepository.save(any(KnowledgeFolder.class))).thenAnswer(invocation -> {
            KnowledgeFolder f = invocation.getArgument(0);
            f.setId(30L);
            return f;
        });

        FolderResponse response = folderService.createFolder(user, request);

        assertNotNull(response);
        assertEquals("Operating Systems", response.name());
        assertNull(response.parentId());
        verify(folderRepository, times(1)).save(any(KnowledgeFolder.class));
    }

    @Test
    void getFolderTree_ReturnsHierarchicalStructure() {
        when(folderRepository.findByUserOrderByNameAsc(user)).thenReturn(List.of(rootFolder, childFolder));

        List<FolderTreeResponse> tree = folderService.getFolderTree(user);

        assertNotNull(tree);
        assertEquals(1, tree.size()); // only 1 root
        assertEquals(rootFolder.getId(), tree.get(0).id());
        assertEquals(1, tree.get(0).children().size());
        assertEquals(childFolder.getId(), tree.get(0).children().get(0).id());
    }

    @Test
    void moveFolder_IntoItself_ThrowsInvalidPayloadException() {
        when(folderRepository.findByUserAndId(user, 10L)).thenReturn(Optional.of(rootFolder));

        FolderMoveRequest moveSelf = new FolderMoveRequest(10L);

        assertThrows(InvalidPayloadException.class, () -> folderService.moveFolder(user, 10L, moveSelf));
    }

    @Test
    void moveFolder_IntoDescendant_CausesCycle_ThrowsInvalidPayloadException() {
        when(folderRepository.findByUserAndId(user, 10L)).thenReturn(Optional.of(rootFolder));
        when(folderRepository.findByUserAndId(user, 20L)).thenReturn(Optional.of(childFolder));

        // Attempt to make childFolder the parent of rootFolder
        FolderMoveRequest moveIntoChild = new FolderMoveRequest(20L);

        InvalidPayloadException ex = assertThrows(InvalidPayloadException.class, () ->
                folderService.moveFolder(user, 10L, moveIntoChild)
        );
        assertTrue(ex.getMessage().contains("circular hierarchy"));
    }

    @Test
    void deleteFolder_ReassignsContainedResourcesToInbox() {
        when(folderRepository.findByUserAndId(user, 20L)).thenReturn(Optional.of(childFolder));
        when(folderRepository.findByUserAndParentOrderByNameAsc(user, childFolder)).thenReturn(Collections.emptyList());
        when(documentRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, 20L)).thenReturn(Collections.emptyList());
        when(knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, 20L)).thenReturn(Collections.emptyList());

        FolderDeleteRequest deleteRequest = new FolderDeleteRequest("INBOX", null);
        folderService.deleteFolder(user, 20L, deleteRequest);

        verify(folderRepository, times(1)).delete(childFolder);
    }
}
