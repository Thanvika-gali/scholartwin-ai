package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.InsightDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.InsightCategory;
import com.aischolartwin.entity.intelligence.InsightStatus;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.InsightEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence/insights")
public class InsightController {

    private final InsightEngineService insightService;
    private final UserService userService;

    public InsightController(InsightEngineService insightService, UserService userService) {
        this.insightService = insightService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<InsightDtos.InsightResponse>> getInsights(
            Authentication authentication,
            @RequestParam(required = false) InsightCategory category,
            @RequestParam(required = false) InsightStatus status
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(insightService.getInsights(user, category, status));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<InsightDtos.InsightResponse> getInsight(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(insightService.getInsight(user, id));
    }

    @PostMapping("/generate")
    public ResponseEntity<InsightDtos.GenerateInsightsResponse> generateInsights(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(insightService.generateInsights(user, forceRefresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<InsightDtos.GenerateInsightsResponse> refreshInsights(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(insightService.generateInsights(user, true));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<InsightDtos.InsightResponse> dismissInsight(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(insightService.dismissInsight(user, id));
    }
}
