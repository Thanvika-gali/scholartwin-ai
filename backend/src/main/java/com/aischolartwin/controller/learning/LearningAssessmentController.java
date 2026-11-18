package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.AssessmentDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningAssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/assessments")
public class LearningAssessmentController {

    private final LearningAssessmentService assessmentService;
    private final UserService userService;

    public LearningAssessmentController(LearningAssessmentService assessmentService, UserService userService) {
        this.assessmentService = assessmentService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<AssessmentDtos.AssessmentSummaryDto>> getAssessments(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(assessmentService.getAssessments(user));
    }

    @PostMapping
    public ResponseEntity<AssessmentDtos.AssessmentSummaryDto> generateAssessment(
            Authentication authentication,
            @Valid @RequestBody AssessmentDtos.GenerateAssessmentRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        AssessmentDtos.AssessmentSummaryDto response = assessmentService.createOrGenerateAssessment(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AssessmentDtos.AssessmentAttemptSummaryDto>> getAttemptHistory(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(assessmentService.getAttemptHistory(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDtos.AssessmentDeliveryResponse> getAssessmentForDelivery(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(assessmentService.getAssessmentForTestTaking(user, id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssessmentDtos.AssessmentResultResponse> submitAssessment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AssessmentDtos.SubmitAssessmentRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(assessmentService.submitAssessment(user, id, request));
    }
}
