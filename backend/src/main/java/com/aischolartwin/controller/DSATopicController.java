package com.aischolartwin.controller;

import com.aischolartwin.dto.DSAProblemResponse;
import com.aischolartwin.dto.DSATopicProgressResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DSAAnalyticsService;
import com.aischolartwin.service.DSAProblemService;
import com.aischolartwin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dsa/topics")
public class DSATopicController {

    private final DSAAnalyticsService analyticsService;
    private final DSAProblemService problemService;
    private final UserService userService;

    public DSATopicController(
            DSAAnalyticsService analyticsService,
            DSAProblemService problemService,
            UserService userService
    ) {
        this.analyticsService = analyticsService;
        this.problemService = problemService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<DSATopicProgressResponse>> listTopicProgress(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<DSATopicProgressResponse> topics = analyticsService.getTopicProgressList(user);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{topic}/problems")
    public ResponseEntity<List<DSAProblemResponse>> getProblemsByTopic(
            Authentication authentication,
            @PathVariable String topic
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DSAProblemResponse> problems = problemService.getProblemsByTopic(user, topic);
        return ResponseEntity.ok(problems);
    }
}

