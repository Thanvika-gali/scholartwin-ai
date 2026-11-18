package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.IntelligenceAnalyticsDtos.*;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.IntelligenceAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intelligence/analytics")
public class IntelligenceAnalyticsController {

    private final IntelligenceAnalyticsService analyticsService;
    private final UserService userService;

    public IntelligenceAnalyticsController(IntelligenceAnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping("/overview")
    public ResponseEntity<IntelligenceOverviewResponse> getOverview(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getOverview(user));
    }

    @GetMapping("/learning")
    public ResponseEntity<LearningIntelligenceDto> getLearningAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getLearningIntelligence(user));
    }

    @GetMapping("/dsa")
    public ResponseEntity<DSAIntelligenceDto> getDsaAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getDsaIntelligence(user));
    }

    @GetMapping("/career")
    public ResponseEntity<CareerIntelligenceDto> getCareerAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getCareerIntelligence(user));
    }

    @GetMapping("/ai-usage")
    public ResponseEntity<AIUsageIntelligenceDto> getAiUsageAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(analyticsService.getAiUsageIntelligence(user));
    }
}
