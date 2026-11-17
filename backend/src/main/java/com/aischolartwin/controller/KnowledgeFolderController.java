package com.aischolartwin.controller;

import com.aischolartwin.dto.FolderDeleteRequest;
import com.aischolartwin.dto.FolderMoveRequest;
import com.aischolartwin.dto.FolderRequest;
import com.aischolartwin.dto.FolderResponse;
import com.aischolartwin.dto.FolderTreeResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeFolderService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/folders")
public class KnowledgeFolderController {

    private final KnowledgeFolderService folderService;
    private final UserService userService;

    public KnowledgeFolderController(KnowledgeFolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getFolders(
            Authentication authentication,
            @RequestParam(value = "tree", defaultValue = "false") boolean tree
    ) {
        User user = userService.getByEmail(authentication.getName());
        if (tree) {
            List<FolderTreeResponse> treeResult = folderService.getFolderTree(user);
            return ResponseEntity.ok(treeResult);
        } else {
            List<FolderResponse> listResult = folderService.listFolders(user);
            return ResponseEntity.ok(listResult);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderResponse> getFolder(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        FolderResponse response = folderService.getFolder(user, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(
            Authentication authentication,
            @Valid @RequestBody FolderRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        FolderResponse response = folderService.createFolder(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderResponse> updateFolder(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FolderRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        FolderResponse response = folderService.updateFolder(user, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<FolderResponse> moveFolder(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody FolderMoveRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        FolderResponse response = folderService.moveFolder(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) FolderDeleteRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        folderService.deleteFolder(user, id, request != null ? request : new FolderDeleteRequest("INBOX", null));
        return ResponseEntity.noContent().build();
    }
}

