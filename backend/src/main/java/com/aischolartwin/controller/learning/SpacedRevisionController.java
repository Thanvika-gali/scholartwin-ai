package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.SpacedRevisionDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.SpacedRevisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning/revisions")
public class SpacedRevisionController {

    private final SpacedRevisionService revisionService;
    private final UserService userService;

    public SpacedRevisionController(SpacedRevisionService revisionService, UserService userService) {
        this.revisionService = revisionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<SpacedRevisionDtos.RevisionQueueSummaryResponse> getRevisionQueue(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(revisionService.getRevisionQueue(user));
    }

    @PostMapping
    public ResponseEntity<SpacedRevisionDtos.SpacedRevisionResponse> createRevisionItem(
            Authentication authentication,
            @Valid @RequestBody SpacedRevisionDtos.CreateRevisionItemRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        SpacedRevisionDtos.SpacedRevisionResponse response = revisionService.createRevisionItem(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SpacedRevisionDtos.SpacedRevisionResponse> completeRevision(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SpacedRevisionDtos.CompleteRevisionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(revisionService.completeRevision(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevisionItem(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        revisionService.deleteRevisionItem(user, id);
        return ResponseEntity.noContent().build();
    }
}
