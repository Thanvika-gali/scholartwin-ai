package com.aischolartwin.controller;

import com.aischolartwin.dto.DSAAttemptRequest;
import com.aischolartwin.dto.DSAAttemptResponse;
import com.aischolartwin.dto.DSAAttemptStatsResponse;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DSAAttemptService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dsa/problems/{problemId}/attempts")
public class DSAAttemptController {

    private final DSAAttemptService attemptService;
    private final UserService userService;

    public DSAAttemptController(DSAAttemptService attemptService, UserService userService) {
        this.attemptService = attemptService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<DSAAttemptResponse> logAttempt(
            Authentication authentication,
            @PathVariable Long problemId,
            @Valid @RequestBody DSAAttemptRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSAAttemptResponse response = attemptService.logAttempt(user, problemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DSAAttemptResponse>> getAttempts(
            Authentication authentication,
            @PathVariable Long problemId
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DSAAttemptResponse> attempts = attemptService.getAttemptsForProblem(user, problemId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/stats")
    public ResponseEntity<DSAAttemptStatsResponse> getStats(
            Authentication authentication,
            @PathVariable Long problemId
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSAAttemptStatsResponse stats = attemptService.getProblemStats(user, problemId);
        return ResponseEntity.ok(stats);
    }
}

