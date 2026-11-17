package com.aischolartwin.controller;

import com.aischolartwin.dto.OrganizationRuleRequest;
import com.aischolartwin.dto.OrganizationRuleResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeRuleService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/rules")
public class KnowledgeRuleController {

    private final KnowledgeRuleService ruleService;
    private final UserService userService;

    public KnowledgeRuleController(KnowledgeRuleService ruleService, UserService userService) {
        this.ruleService = ruleService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationRuleResponse>> listRules(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(ruleService.listRules(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationRuleResponse> getRule(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(ruleService.getRule(user, id));
    }

    @PostMapping
    public ResponseEntity<OrganizationRuleResponse> createRule(
            Authentication authentication,
            @Valid @RequestBody OrganizationRuleRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        OrganizationRuleResponse response = ruleService.createRule(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationRuleResponse> updateRule(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRuleRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        OrganizationRuleResponse response = ruleService.updateRule(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        ruleService.deleteRule(user, id);
        return ResponseEntity.noContent().build();
    }
}

