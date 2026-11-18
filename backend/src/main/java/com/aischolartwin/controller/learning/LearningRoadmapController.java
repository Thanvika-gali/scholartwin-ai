package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.RoadmapDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningRoadmapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class LearningRoadmapController {

    private final LearningRoadmapService roadmapService;
    private final UserService userService;

    public LearningRoadmapController(LearningRoadmapService roadmapService, UserService userService) {
        this.roadmapService = roadmapService;
        this.userService = userService;
    }

    @GetMapping("/roadmaps")
    public ResponseEntity<List<RoadmapDtos.LearningRoadmapResponse>> getRoadmaps(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(roadmapService.getRoadmaps(user));
    }

    @PostMapping("/roadmaps")
    public ResponseEntity<RoadmapDtos.LearningRoadmapResponse> createRoadmap(
            Authentication authentication,
            @Valid @RequestBody RoadmapDtos.CreateRoadmapRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        RoadmapDtos.LearningRoadmapResponse response = roadmapService.createOrGenerateRoadmap(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/roadmaps/{id}")
    public ResponseEntity<RoadmapDtos.LearningRoadmapResponse> getRoadmapDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(roadmapService.getRoadmapDetail(user, id));
    }

    @PutMapping("/roadmaps/{id}")
    public ResponseEntity<RoadmapDtos.LearningRoadmapResponse> updateRoadmap(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody RoadmapDtos.UpdateRoadmapRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(roadmapService.updateRoadmap(user, id, request));
    }

    @DeleteMapping("/roadmaps/{id}")
    public ResponseEntity<Void> deleteRoadmap(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        roadmapService.deleteRoadmap(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roadmaps/phases/{phaseId}/tasks")
    public ResponseEntity<RoadmapDtos.LearningTaskResponse> addTaskToPhase(
            Authentication authentication,
            @PathVariable Long phaseId,
            @Valid @RequestBody RoadmapDtos.CreateTaskRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        RoadmapDtos.LearningTaskResponse response = roadmapService.addTaskToPhase(user, phaseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<RoadmapDtos.LearningTaskResponse> updateTaskStatus(
            Authentication authentication,
            @PathVariable Long taskId,
            @Valid @RequestBody RoadmapDtos.UpdateTaskStatusRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(roadmapService.updateTaskStatus(user, taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            Authentication authentication,
            @PathVariable Long taskId
    ) {
        User user = userService.getByEmail(authentication.getName());
        roadmapService.deleteTask(user, taskId);
        return ResponseEntity.noContent().build();
    }
}
