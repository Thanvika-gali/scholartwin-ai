package com.aischolartwin.service;

import com.aischolartwin.dto.FolderDeleteRequest;
import com.aischolartwin.dto.FolderMoveRequest;
import com.aischolartwin.dto.FolderRequest;
import com.aischolartwin.dto.FolderResponse;
import com.aischolartwin.dto.FolderTreeResponse;
import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.KnowledgeFolder;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.KnowledgeFolderRepository;
import com.aischolartwin.repository.KnowledgeItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeFolderService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeFolderService.class);

    private final KnowledgeFolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;

    public KnowledgeFolderService(KnowledgeFolderRepository folderRepository,
                                  DocumentRepository documentRepository,
                                  KnowledgeItemRepository knowledgeItemRepository) {
        this.folderRepository = folderRepository;
        this.documentRepository = documentRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> listFolders(User user) {
        List<KnowledgeFolder> all = folderRepository.findByUserOrderByNameAsc(user);
        return all.stream().map(f -> toFolderResponse(user, f)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FolderTreeResponse> getFolderTree(User user) {
        List<KnowledgeFolder> all = folderRepository.findByUserOrderByNameAsc(user);
        Map<Long, List<KnowledgeFolder>> byParentId = new HashMap<>();
        List<KnowledgeFolder> roots = new ArrayList<>();

        for (KnowledgeFolder f : all) {
            if (f.getParent() == null) {
                roots.add(f);
            } else {
                byParentId.computeIfAbsent(f.getParent().getId(), k -> new ArrayList<>()).add(f);
            }
        }

        return roots.stream()
                .map(r -> buildTreeNode(user, r, byParentId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FolderResponse getFolder(User user, Long id) {
        KnowledgeFolder folder = findUserFolderOrThrow(user, id);
        return toFolderResponse(user, folder);
    }

    @Transactional
    public FolderResponse createFolder(User user, FolderRequest request) {
        KnowledgeFolder parent = null;
        if (request.parentId() != null) {
            parent = findUserFolderOrThrow(user, request.parentId());
        }

        KnowledgeFolder folder = new KnowledgeFolder(
                user,
                parent,
                request.name(),
                request.description(),
                request.icon(),
                request.color()
        );

        if (request.isPinned() != null) folder.setPinned(request.isPinned());
        if (request.isFavorite() != null) folder.setFavorite(request.isFavorite());

        folder = folderRepository.save(folder);
        log.info("Created folder '{}' (id: {}) for user {}", folder.getName(), folder.getId(), user.getEmail());
        return toFolderResponse(user, folder);
    }

    @Transactional
    public FolderResponse updateFolder(User user, Long id, FolderRequest request) {
        KnowledgeFolder folder = findUserFolderOrThrow(user, id);

        if (request.name() != null && !request.name().isBlank()) {
            folder.setName(request.name());
        }
        if (request.description() != null) {
            folder.setDescription(request.description());
        }
        if (request.icon() != null && !request.icon().isBlank()) {
            folder.setIcon(request.icon());
        }
        if (request.color() != null && !request.color().isBlank()) {
            folder.setColor(request.color());
        }
        if (request.isPinned() != null) {
            folder.setPinned(request.isPinned());
        }
        if (request.isFavorite() != null) {
            folder.setFavorite(request.isFavorite());
        }

        folder = folderRepository.save(folder);
        return toFolderResponse(user, folder);
    }

    @Transactional
    public FolderResponse moveFolder(User user, Long id, FolderMoveRequest request) {
        KnowledgeFolder folder = findUserFolderOrThrow(user, id);

        if (request.targetParentId() == null) {
            folder.setParent(null);
        } else {
            if (request.targetParentId().equals(id)) {
                throw new InvalidPayloadException("Cannot move folder into itself.");
            }
            KnowledgeFolder newParent = findUserFolderOrThrow(user, request.targetParentId());

            // Cycle prevention: verify newParent is not a descendant of folder
            KnowledgeFolder current = newParent;
            while (current != null) {
                if (current.getId().equals(id)) {
                    throw new InvalidPayloadException("Cannot move a folder into its own descendant (would cause a circular hierarchy).");
                }
                current = current.getParent();
            }
            folder.setParent(newParent);
        }

        folder = folderRepository.save(folder);
        return toFolderResponse(user, folder);
    }

    @Transactional
    public void deleteFolder(User user, Long id, FolderDeleteRequest request) {
        KnowledgeFolder folder = findUserFolderOrThrow(user, id);

        // Determine destination for contained resources
        Long reassignFolderId = null;
        String option = request != null && request.reassignOption() != null ? request.reassignOption().toUpperCase() : "INBOX";

        if ("PARENT".equals(option)) {
            reassignFolderId = folder.getParent() != null ? folder.getParent().getId() : null;
        } else if ("TARGET_FOLDER".equals(option) && request.targetFolderId() != null) {
            if (!request.targetFolderId().equals(id)) {
                findUserFolderOrThrow(user, request.targetFolderId()); // ensure exists & owned
                reassignFolderId = request.targetFolderId();
            }
        } // else "INBOX" -> reassignFolderId stays null

        // Reassign all documents in this folder
        List<Document> docs = documentRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, id);
        for (Document doc : docs) {
            doc.setFolderId(reassignFolderId);
            documentRepository.save(doc);
        }

        // Reassign all knowledge items in this folder
        List<KnowledgeItem> items = knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, id);
        for (KnowledgeItem item : items) {
            item.setFolderId(reassignFolderId);
            knowledgeItemRepository.save(item);
        }

        // Reparent child folders to this folder's parent
        List<KnowledgeFolder> children = folderRepository.findByUserAndParentOrderByNameAsc(user, folder);
        for (KnowledgeFolder child : children) {
            child.setParent(folder.getParent());
            folderRepository.save(child);
        }

        folderRepository.delete(folder);
        log.info("Safely deleted folder '{}' (id: {}). Reassigned {} docs and {} items to folderId: {}",
                folder.getName(), id, docs.size(), items.size(), reassignFolderId);
    }

    public KnowledgeFolder findUserFolderOrThrow(User user, Long id) {
        return folderRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found or access denied (id: " + id + ")"));
    }

    public String computeFolderPath(KnowledgeFolder folder) {
        if (folder == null) return "Smart Inbox";
        List<String> names = new ArrayList<>();
        KnowledgeFolder cur = folder;
        while (cur != null) {
            names.add(cur.getName());
            cur = cur.getParent();
        }
        Collections.reverse(names);
        return String.join(" / ", names);
    }

    private FolderTreeResponse buildTreeNode(User user, KnowledgeFolder folder, Map<Long, List<KnowledgeFolder>> byParentId) {
        long docCount = documentRepository.countByUserAndFolderId(user, folder.getId());
        long noteCount = knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, folder.getId()).stream()
                .filter(i -> i.getType() == KnowledgeItemType.NOTE).count();
        long linkCount = knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, folder.getId()).stream()
                .filter(i -> i.getType() == KnowledgeItemType.LINK).count();

        List<KnowledgeFolder> childFolders = byParentId.getOrDefault(folder.getId(), Collections.emptyList());
        List<FolderTreeResponse> children = childFolders.stream()
                .map(c -> buildTreeNode(user, c, byParentId))
                .collect(Collectors.toList());

        return new FolderTreeResponse(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                folder.getDescription(),
                folder.getIcon(),
                folder.getColor(),
                folder.isPinned(),
                folder.isFavorite(),
                docCount,
                noteCount,
                linkCount,
                docCount + noteCount + linkCount,
                children,
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

    private FolderResponse toFolderResponse(User user, KnowledgeFolder f) {
        long docCount = documentRepository.countByUserAndFolderId(user, f.getId());
        long noteCount = knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, f.getId()).stream()
                .filter(i -> i.getType() == KnowledgeItemType.NOTE).count();
        long linkCount = knowledgeItemRepository.findByUserAndFolderIdOrderByUpdatedAtDesc(user, f.getId()).stream()
                .filter(i -> i.getType() == KnowledgeItemType.LINK).count();

        return new FolderResponse(
                f.getId(),
                f.getName(),
                f.getParent() != null ? f.getParent().getId() : null,
                f.getParent() != null ? f.getParent().getName() : null,
                f.getDescription(),
                f.getIcon(),
                f.getColor(),
                f.isPinned(),
                f.isFavorite(),
                docCount,
                noteCount,
                linkCount,
                docCount + noteCount + linkCount,
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}

