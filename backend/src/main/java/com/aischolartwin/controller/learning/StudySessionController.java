package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.StudySessionDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/study-sessions")
public class StudySessionController {

    private final StudySessionService sessionService;
    private final UserService userService;

    public StudySessionController(StudySessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<StudySessionDtos.StudySessionResponse>> getSessions(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(sessionService.getSessions(user));
    }

    @PostMapping
    public ResponseEntity<StudySessionDtos.StudySessionResponse> logSession(
            Authentication authentication,
            @Valid @RequestBody StudySessionDtos.LogStudySessionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        StudySessionDtos.StudySessionResponse response = sessionService.logSession(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
