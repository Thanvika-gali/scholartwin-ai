package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.LearningAnalyticsDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning/analytics")
public class LearningAnalyticsController {

    private final LearningAnalyticsService analyticsService;
    private final UserService userService;

    public LearningAnalyticsController(LearningAnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<LearningAnalyticsDto.Response> getAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getAnalytics(user));
    }
}
