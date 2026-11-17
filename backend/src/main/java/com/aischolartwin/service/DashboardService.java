package com.aischolartwin.service;

import com.aischolartwin.dto.DashboardSummaryResponse;
import com.aischolartwin.dto.KnowledgeItemResponse;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.UserNotFoundException;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing summary metrics and recent learning items for the personalized dashboard.
 */
@Service
public class DashboardService {

    private final KnowledgeItemRepository knowledgeItemRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DashboardService(
        KnowledgeItemRepository knowledgeItemRepository,
        DocumentRepository documentRepository,
        UserRepository userRepository
    ) {
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        long totalNotes = knowledgeItemRepository.countByUserAndType(user, KnowledgeItemType.NOTE);
        long totalLinks = knowledgeItemRepository.countByUserAndType(user, KnowledgeItemType.LINK);
        long totalDocuments = documentRepository.countByUser(user);
        long totalItems = totalNotes + totalLinks + totalDocuments;

        List<KnowledgeItem> recentItems = knowledgeItemRepository.findTop5ByUserOrderByUpdatedAtDesc(user);
        List<KnowledgeItemResponse> recentResponses = recentItems.stream()
                .map(KnowledgeItemResponse::fromEntity)
                .toList();

        return new DashboardSummaryResponse(
            totalItems,
            totalNotes,
            totalLinks,
            totalDocuments,
            recentResponses
        );
    }
}
