package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.RecommendationDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.RecommendationStatus;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.RecommendationEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence/recommendations")
public class RecommendationController {

    private final RecommendationEngineService recommendationService;
    private final UserService userService;

    public RecommendationController(RecommendationEngineService recommendationService, UserService userService) {
        this.recommendationService = recommendationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationDtos.RecommendationResponse>> getRecommendations(
            Authentication authentication,
            @RequestParam(required = false) RecommendationStatus status
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(recommendationService.getRecommendations(user, status));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<RecommendationDtos.RecommendationResponse>> generateRecommendations(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(recommendationService.generateRecommendations(user));
    }

    @PostMapping("/{id:\\d+}/accept")
    public ResponseEntity<RecommendationDtos.RecommendationResponse> acceptRecommendation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(recommendationService.acceptRecommendation(user, id));
    }

    @PostMapping("/{id:\\d+}/dismiss")
    public ResponseEntity<RecommendationDtos.RecommendationResponse> dismissRecommendation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(recommendationService.dismissRecommendation(user, id));
    }

    @PostMapping("/{id:\\d+}/complete")
    public ResponseEntity<RecommendationDtos.RecommendationResponse> completeRecommendation(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(recommendationService.completeRecommendation(user, id));
    }
}
