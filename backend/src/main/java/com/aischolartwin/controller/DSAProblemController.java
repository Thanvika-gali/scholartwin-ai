package com.aischolartwin.controller;

import com.aischolartwin.dto.DSAProblemDetailResponse;
import com.aischolartwin.dto.DSAProblemRequest;
import com.aischolartwin.dto.DSAProblemResponse;
import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DSAProblemService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dsa/problems")
public class DSAProblemController {

    private final DSAProblemService problemService;
    private final UserService userService;

    public DSAProblemController(DSAProblemService problemService, UserService userService) {
        this.problemService = problemService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<DSAProblemResponse> createProblem(
            Authentication authentication,
            @Valid @RequestBody DSAProblemRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSAProblemResponse response = problemService.createProblem(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DSAProblemResponse>> listProblems(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) DSADifficulty difficulty,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) DSAStatus status,
            @RequestParam(required = false) Boolean revisionRequired,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        User user = userService.getByEmail(authentication.getName());

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = ("title".equalsIgnoreCase(sortBy) || "createdAt".equalsIgnoreCase(sortBy))
                ? sortBy : "updatedAt";

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(direction, sortProperty));

        Page<DSAProblemResponse> result = problemService.searchProblems(
                user, search, topic, difficulty, platform, status, revisionRequired, pageable
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DSAProblemDetailResponse> getProblemDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSAProblemDetailResponse response = problemService.getProblemDetail(user, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DSAProblemResponse> updateProblem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DSAProblemRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        DSAProblemResponse response = problemService.updateProblem(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        problemService.deleteProblem(user, id);
        return ResponseEntity.noContent().build();
    }
}

