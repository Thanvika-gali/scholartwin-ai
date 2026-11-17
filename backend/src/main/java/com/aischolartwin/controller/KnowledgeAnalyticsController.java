package com.aischolartwin.controller;

import com.aischolartwin.dto.KnowledgeAnalyticsResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeAnalyticsService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge/analytics")
public class KnowledgeAnalyticsController {

    private final KnowledgeAnalyticsService analyticsService;
    private final UserService userService;

    public KnowledgeAnalyticsController(KnowledgeAnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<KnowledgeAnalyticsResponse> getAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getAnalytics(user));
    }
}

