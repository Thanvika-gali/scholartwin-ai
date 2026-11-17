package com.aischolartwin.controller;

import com.aischolartwin.dto.DSARevisionQueueResponse;
import com.aischolartwin.dto.DSARevisionRequest;
import com.aischolartwin.dto.DSARevisionResponse;
import com.aischolartwin.dto.DSARevisionUpdateRequest;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DSARevisionService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dsa")
public class DSARevisionController {

    private final DSARevisionService revisionService;
    private final UserService userService;

    public DSARevisionController(DSARevisionService revisionService, UserService userService) {
        this.revisionService = revisionService;
        this.userService = userService;
    }

    @GetMapping("/revisions")
    public ResponseEntity<DSARevisionQueueResponse> getRevisionQueue(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        DSARevisionQueueResponse response = revisionService.getRevisionQueue(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/problems/{problemId}/revisions")
    public ResponseEntity<DSARevisionResponse> scheduleRevision(
            Authentication authentication,
            @PathVariable Long problemId,
            @Valid @RequestBody DSARevisionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSARevisionResponse response = revisionService.scheduleRevision(user, problemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/problems/{problemId}/revisions")
    public ResponseEntity<List<DSARevisionResponse>> getRevisionsForProblem(
            Authentication authentication,
            @PathVariable Long problemId
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DSARevisionResponse> responses = revisionService.getRevisionsForProblem(user, problemId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/revisions/{revisionId}")
    public ResponseEntity<DSARevisionResponse> updateRevision(
            Authentication authentication,
            @PathVariable Long revisionId,
            @RequestBody DSARevisionUpdateRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSARevisionResponse response = revisionService.updateRevision(user, revisionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/revisions/{revisionId}")
    public ResponseEntity<Void> deleteRevision(
            Authentication authentication,
            @PathVariable Long revisionId
    ) {
        User user = userService.getByEmail(authentication.getName());
        revisionService.deleteRevision(user, revisionId);
        return ResponseEntity.noContent().build();
    }
}

