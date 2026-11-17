package com.aischolartwin.controller;

import com.aischolartwin.dto.KnowledgeSearchResponse;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeSearchService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge/search")
public class KnowledgeSearchController {

    private final KnowledgeSearchService searchService;
    private final UserService userService;

    public KnowledgeSearchController(KnowledgeSearchService searchService, UserService userService) {
        this.searchService = searchService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<KnowledgeSearchResponse> search(
            Authentication authentication,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false, defaultValue = "false") Boolean favoriteOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean archivedOnly,
            @RequestParam(required = false, defaultValue = "RECENT_MODIFIED") String sortBy
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(searchService.search(user, query, type, folderId, favoriteOnly, archivedOnly, sortBy));
    }
}

