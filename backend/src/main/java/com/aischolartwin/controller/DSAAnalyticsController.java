package com.aischolartwin.controller;

import com.aischolartwin.dto.DSAActivityTimelineItem;
import com.aischolartwin.dto.DSADashboardSummaryResponse;
import com.aischolartwin.dto.DSADifficultyAnalyticsResponse;
import com.aischolartwin.dto.DSATopicProgressResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DSAAnalyticsService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dsa")
public class DSAAnalyticsController {

    private final DSAAnalyticsService analyticsService;
    private final UserService userService;

    public DSAAnalyticsController(DSAAnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DSADashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        DSADashboardSummaryResponse summary = analyticsService.getDashboardSummary(user);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/analytics/difficulty")
    public ResponseEntity<DSADifficultyAnalyticsResponse> getDifficultyAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        DSADifficultyAnalyticsResponse analytics = analyticsService.getDifficultyAnalytics(user);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/analytics/topics")
    public ResponseEntity<List<DSATopicProgressResponse>> getTopicsAnalytics(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<DSATopicProgressResponse> topics = analyticsService.getTopicProgressList(user);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/analytics/activity")
    public ResponseEntity<List<DSAActivityTimelineItem>> getActivityTimeline(
            Authentication authentication,
            @RequestParam(defaultValue = "14") int days
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DSAActivityTimelineItem> timeline = analyticsService.getActivityTimeline(user, days);
        return ResponseEntity.ok(timeline);
    }
}

