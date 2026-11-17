package com.aischolartwin.controller;

import com.aischolartwin.dto.JobDescriptionAnalysisRequest;
import com.aischolartwin.dto.JobDescriptionAnalysisResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.JobDescriptionService;
import com.aischolartwin.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career/job-descriptions")
public class CareerJobDescriptionController {

    private final JobDescriptionService jobDescriptionService;
    private final UserService userService;

    public CareerJobDescriptionController(JobDescriptionService jobDescriptionService, UserService userService) {
        this.jobDescriptionService = jobDescriptionService;
        this.userService = userService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<JobDescriptionAnalysisResponse> analyzeJobDescription(
            Authentication authentication,
            @RequestBody JobDescriptionAnalysisRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        JobDescriptionAnalysisResponse response = jobDescriptionService.analyzeJobDescription(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobDescriptionAnalysisResponse>> listJobAnalyses(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<JobDescriptionAnalysisResponse> list = jobDescriptionService.getUserJobAnalyses(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDescriptionAnalysisResponse> getJobAnalysisById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        JobDescriptionAnalysisResponse response = jobDescriptionService.getJobAnalysisById(user, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobAnalysis(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        jobDescriptionService.deleteJobAnalysis(user, id);
        return ResponseEntity.noContent().build();
    }
}

