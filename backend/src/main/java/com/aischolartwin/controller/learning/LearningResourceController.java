package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.LearningResourceDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning/resources")
public class LearningResourceController {

    private final LearningResourceService resourceService;
    private final UserService userService;

    public LearningResourceController(LearningResourceService resourceService, UserService userService) {
        this.resourceService = resourceService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<LearningResourceDtos.LearningResourceResponse>> getResources(
            Authentication authentication,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Long taskId
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(resourceService.getResources(user, topic, taskId));
    }

    @PostMapping
    public ResponseEntity<LearningResourceDtos.LearningResourceResponse> associateResource(
            Authentication authentication,
            @Valid @RequestBody LearningResourceDtos.AssociateResourceRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        LearningResourceDtos.LearningResourceResponse response = resourceService.associateResource(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/studied")
    public ResponseEntity<LearningResourceDtos.LearningResourceResponse> markStudied(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        User user = userService.getByEmail(authentication.getName());
        boolean studied = body.getOrDefault("studied", true);
        return ResponseEntity.ok(resourceService.markStudied(user, id, studied));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        resourceService.deleteResource(user, id);
        return ResponseEntity.noContent().build();
    }
}
