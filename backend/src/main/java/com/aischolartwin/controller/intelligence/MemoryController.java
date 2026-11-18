package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.MemoryCategory;
import com.aischolartwin.entity.intelligence.MemoryStatus;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.MemoryExtractionService;
import com.aischolartwin.service.intelligence.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence/memories")
public class MemoryController {

    private final MemoryService memoryService;
    private final MemoryExtractionService extractionService;
    private final UserService userService;

    public MemoryController(MemoryService memoryService,
                            MemoryExtractionService extractionService,
                            UserService userService) {
        this.memoryService = memoryService;
        this.extractionService = extractionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<MemoryDtos.MemoryResponse>> getMemories(
            Authentication authentication,
            @RequestParam(required = false) MemoryCategory category,
            @RequestParam(required = false) MemoryStatus status,
            @RequestParam(required = false) Boolean pinnedOnly
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.getMemories(user, category, status, pinnedOnly));
    }

    @GetMapping("/stats")
    public ResponseEntity<MemoryDtos.MemoryStatsResponse> getStats(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.getMemoryStats(user));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<MemoryDtos.MemoryResponse> getMemory(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.getMemory(user, id));
    }

    @PostMapping
    public ResponseEntity<MemoryDtos.MemoryResponse> createMemory(
            Authentication authentication,
            @Valid @RequestBody MemoryDtos.CreateMemoryRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        MemoryDtos.MemoryResponse response = memoryService.createMemory(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoryDtos.MemoryResponse> updateMemory(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody MemoryDtos.UpdateMemoryRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.updateMemory(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemory(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        memoryService.deleteMemory(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<MemoryDtos.MemoryResponse> archiveMemory(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.archiveMemory(user, id));
    }

    @PostMapping("/{id}/pin")
    public ResponseEntity<MemoryDtos.MemoryResponse> togglePin(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.togglePin(user, id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemoryDtos.MemoryResponse>> searchMemories(
            Authentication authentication,
            @RequestParam(required = false) String q
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(memoryService.searchMemories(user, q));
    }

    @PostMapping("/extract")
    public ResponseEntity<MemoryDtos.MemoryExtractionResult> extractMemories(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(extractionService.extractMemoriesFromActivity(user));
    }

    @PostMapping("/consolidate")
    public ResponseEntity<MemoryDtos.MemoryExtractionResult> consolidateMemories(
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(extractionService.consolidateMemories(user));
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDelete(
            Authentication authentication,
            @RequestBody MemoryDtos.BulkDeleteRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        memoryService.bulkDelete(user, request);
        return ResponseEntity.noContent().build();
    }
}
