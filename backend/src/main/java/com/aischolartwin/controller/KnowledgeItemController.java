package com.aischolartwin.controller;

import com.aischolartwin.dto.KnowledgeItemRequest;
import com.aischolartwin.dto.KnowledgeItemResponse;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.service.KnowledgeItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing Knowledge Hub CRUD endpoints.
 * All operations are scoped strictly to the authenticated principal.
 */
@RestController
@RequestMapping("/api/knowledge-items")
public class KnowledgeItemController {

    private final KnowledgeItemService knowledgeItemService;

    public KnowledgeItemController(KnowledgeItemService knowledgeItemService) {
        this.knowledgeItemService = knowledgeItemService;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeItemResponse>> getItems(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) KnowledgeItemType type,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search
    ) {
        List<KnowledgeItemResponse> items = knowledgeItemService.getItems(userDetails.getUsername(), type, category, search);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeItemResponse> getItemById(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        KnowledgeItemResponse item = knowledgeItemService.getItemById(id, userDetails.getUsername());
        return ResponseEntity.ok(item);
    }

    @PostMapping
    public ResponseEntity<KnowledgeItemResponse> createItem(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody KnowledgeItemRequest request
    ) {
        KnowledgeItemResponse created = knowledgeItemService.createItem(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeItemResponse> updateItem(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody KnowledgeItemRequest request
    ) {
        KnowledgeItemResponse updated = knowledgeItemService.updateItem(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long id
    ) {
        knowledgeItemService.deleteItem(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}

