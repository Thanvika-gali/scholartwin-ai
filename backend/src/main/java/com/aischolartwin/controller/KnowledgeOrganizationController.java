package com.aischolartwin.controller;

import com.aischolartwin.dto.BulkOrganizeRequest;
import com.aischolartwin.dto.OrganizationSuggestionResponse;
import com.aischolartwin.dto.SuggestionActionRequest;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeOrganizationService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeOrganizationController {

    private final KnowledgeOrganizationService organizationService;
    private final UserService userService;

    public KnowledgeOrganizationController(KnowledgeOrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<OrganizationSuggestionResponse>> getSuggestions(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<OrganizationSuggestionResponse> suggestions = organizationService.getPendingSuggestions(user);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/suggestions/{id}/accept")
    public ResponseEntity<OrganizationSuggestionResponse> acceptSuggestion(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody(required = false) SuggestionActionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        OrganizationSuggestionResponse response = organizationService.acceptSuggestion(user, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suggestions/{id}/reject")
    public ResponseEntity<OrganizationSuggestionResponse> rejectSuggestion(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        OrganizationSuggestionResponse response = organizationService.rejectSuggestion(user, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suggestions/{id}/dismiss")
    public ResponseEntity<OrganizationSuggestionResponse> dismissSuggestion(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        OrganizationSuggestionResponse response = organizationService.dismissSuggestion(user, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/organize")
    public ResponseEntity<List<OrganizationSuggestionResponse>> bulkOrganize(
            Authentication authentication,
            @RequestBody BulkOrganizeRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<OrganizationSuggestionResponse> responses = organizationService.bulkOrganize(user, request);
        return ResponseEntity.ok(responses);
    }
}

