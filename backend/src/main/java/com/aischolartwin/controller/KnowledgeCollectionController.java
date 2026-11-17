package com.aischolartwin.controller;

import com.aischolartwin.dto.CollectionItemRequest;
import com.aischolartwin.dto.CollectionRequest;
import com.aischolartwin.dto.CollectionResponse;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.KnowledgeCollectionService;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge/collections")
public class KnowledgeCollectionController {

    private final KnowledgeCollectionService collectionService;
    private final UserService userService;

    public KnowledgeCollectionController(KnowledgeCollectionService collectionService, UserService userService) {
        this.collectionService = collectionService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> listCollections(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(collectionService.listCollections(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> getCollection(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(collectionService.getCollection(user, id));
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(
            Authentication authentication,
            @Valid @RequestBody CollectionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CollectionResponse response = collectionService.createCollection(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponse> updateCollection(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CollectionRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CollectionResponse response = collectionService.updateCollection(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        collectionService.deleteCollection(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CollectionResponse> addItem(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CollectionItemRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        CollectionResponse response = collectionService.addItemToCollection(user, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/items")
    public ResponseEntity<CollectionResponse> removeItem(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("type") ResourceType type,
            @RequestParam("resourceId") Long resourceId
    ) {
        User user = userService.getByEmail(authentication.getName());
        CollectionResponse response = collectionService.removeItemFromCollection(user, id, type, resourceId);
        return ResponseEntity.ok(response);
    }
}

