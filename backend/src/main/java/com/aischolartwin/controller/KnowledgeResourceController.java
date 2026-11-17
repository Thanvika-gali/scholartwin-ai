package com.aischolartwin.controller;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeRelationService;
import com.aischolartwin.service.KnowledgeResourceService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeResourceController {

    private final KnowledgeResourceService resourceService;
    private final KnowledgeRelationService relationService;
    private final UserService userService;

    public KnowledgeResourceController(
            KnowledgeResourceService resourceService,
            KnowledgeRelationService relationService,
            UserService userService) {
        this.resourceService = resourceService;
        this.relationService = relationService;
        this.userService = userService;
    }

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceItemResponse>> listResources(
            Authentication authentication,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "false") Boolean inboxOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean favoritesOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean pinnedOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean archivedOnly,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "RECENT_MODIFIED") String sortBy
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<ResourceItemResponse> resources = resourceService.listResources(
                user, type, folderId, inboxOnly, favoritesOnly, pinnedOnly, archivedOnly, search, sortBy);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<ResourceItemResponse>> getInbox(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<ResourceItemResponse> inboxResources = resourceService.listResources(
                user, null, null, true, false, false, false, null, "RECENT_ADDED");
        return ResponseEntity.ok(inboxResources);
    }

    @GetMapping("/resources/{type}/{id}")
    public ResponseEntity<ResourceItemResponse> getResource(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.getResourceItem(user, type, id));
    }

    @PostMapping("/resources/{type}/{id}/move")
    public ResponseEntity<ResourceItemResponse> moveResource(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id,
            @RequestParam(required = false) Long targetFolderId
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.moveResource(user, type, id, targetFolderId));
    }

    @PostMapping("/resources/{type}/{id}/favorite")
    public ResponseEntity<ResourceItemResponse> toggleFavorite(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.toggleFavorite(user, type, id));
    }

    @PostMapping("/resources/{type}/{id}/pin")
    public ResponseEntity<ResourceItemResponse> togglePin(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.togglePin(user, type, id));
    }

    @PostMapping("/resources/{type}/{id}/archive")
    public ResponseEntity<ResourceItemResponse> toggleArchive(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.toggleArchive(user, type, id));
    }

    @PostMapping("/resources/{type}/{id}/view")
    public ResponseEntity<Void> recordView(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        resourceService.recordView(user, type, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resources/recent")
    public ResponseEntity<List<ResourceItemResponse>> getRecentlyViewed(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.getRecentlyViewed(user));
    }

    @GetMapping("/resources/{type}/{id}/related")
    public ResponseEntity<List<ResourceRelationResponse>> getRelated(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(relationService.getRelatedResources(user, type, id));
    }

    @PostMapping("/resources/{type}/{id}/connect")
    public ResponseEntity<ResourceRelationResponse> connectResource(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id,
            @Valid @RequestBody ResourceRelationRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResourceRelationResponse response = relationService.connectResources(user, type, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/resources/{type}/{id}/connect/{targetType}/{targetId}")
    public ResponseEntity<Void> disconnectResource(
            Authentication authentication,
            @PathVariable ResourceType type,
            @PathVariable Long id,
            @PathVariable ResourceType targetType,
            @PathVariable Long targetId
    ) {
        User user = userService.getByEmail(authentication.getName());
        relationService.disconnectResources(user, type, id, targetType, targetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> executeBulkAction(
            Authentication authentication,
            @RequestBody ResourceBulkActionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        resourceService.executeBulkAction(user, request);
        return ResponseEntity.ok().build();
    }
}

