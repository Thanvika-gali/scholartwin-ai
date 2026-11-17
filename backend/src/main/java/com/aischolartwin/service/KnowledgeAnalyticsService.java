package com.aischolartwin.service;

import com.aischolartwin.dto.FolderResponse;
import com.aischolartwin.dto.KnowledgeAnalyticsResponse;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeFolderRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KnowledgeAnalyticsService {

    private final DocumentRepository documentRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeFolderRepository folderRepository;
    private final KnowledgeFolderService folderService;

    public KnowledgeAnalyticsService(DocumentRepository documentRepository,
                                     KnowledgeItemRepository knowledgeItemRepository,
                                     KnowledgeFolderRepository folderRepository,
                                     KnowledgeFolderService folderService) {
        this.documentRepository = documentRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.folderRepository = folderRepository;
        this.folderService = folderService;
    }

    @Transactional(readOnly = true)
    public KnowledgeAnalyticsResponse getAnalytics(User user) {
        long totalDocs = documentRepository.countByUser(user);
        long totalNotes = knowledgeItemRepository.countByUserAndType(user, KnowledgeItemType.NOTE);
        long totalLinks = knowledgeItemRepository.countByUserAndType(user, KnowledgeItemType.LINK);
        long totalFolders = folderRepository.countByUser(user);

        long inboxDocs = documentRepository.countByUserAndFolderIdIsNullAndIsArchivedFalse(user);
        long inboxItems = knowledgeItemRepository.countByUserAndFolderIdIsNullAndIsArchivedFalse(user);
        long smartInboxCount = inboxDocs + inboxItems;

        long favDocs = documentRepository.countByUserAndIsFavoriteTrue(user);
        long favItems = knowledgeItemRepository.countByUserAndIsFavoriteTrue(user);
        long favoritesCount = favDocs + favItems;

        long archDocs = documentRepository.countByUserAndIsArchivedTrue(user);
        long archItems = knowledgeItemRepository.countByUserAndIsArchivedTrue(user);
        long archivedCount = archDocs + archItems;

        long storageBytes = documentRepository.sumFileSizeByUser(user);

        Map<String, Long> byType = new LinkedHashMap<>();
        byType.put("DOCUMENT", totalDocs);
        byType.put("NOTE", totalNotes);
        byType.put("LINK", totalLinks);

        Map<String, Long> bySubject = new LinkedHashMap<>();
        bySubject.put("Database Systems (DBMS)", 0L);
        bySubject.put("Data Structures & Algorithms", 0L);
        bySubject.put("Operating Systems", 0L);
        bySubject.put("Software Engineering", 0L);
        bySubject.put("General Academic", 0L);

        // Derive active folders
        List<FolderResponse> folders = folderService.listFolders(user);
        folders.sort(Comparator.comparing(FolderResponse::totalCount).reversed());
        List<FolderResponse> topFolders = folders.subList(0, Math.min(5, folders.size()));

        return new KnowledgeAnalyticsResponse(
                totalDocs,
                totalNotes,
                totalLinks,
                totalFolders,
                smartInboxCount,
                favoritesCount,
                archivedCount,
                storageBytes,
                KnowledgeResourceService.formatBytes(storageBytes),
                bySubject,
                byType,
                topFolders
        );
    }
}

