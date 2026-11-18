package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.LearningGapReportDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningGapAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning/gaps")
public class LearningGapController {

    private final LearningGapAnalysisService gapAnalysisService;
    private final UserService userService;

    public LearningGapController(LearningGapAnalysisService gapAnalysisService, UserService userService) {
        this.gapAnalysisService = gapAnalysisService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<LearningGapReportDto.Response> getGapAnalysis(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(gapAnalysisService.analyzeGaps(user));
    }
}
