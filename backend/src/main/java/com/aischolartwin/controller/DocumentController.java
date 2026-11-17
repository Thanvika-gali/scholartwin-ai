package com.aischolartwin.controller;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.DocumentFormat;
import com.aischolartwin.entity.DocumentStatus;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.DocumentIntelligenceService;
import com.aischolartwin.service.DocumentService;
import com.aischolartwin.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentIntelligenceService intelligenceService;
    private final UserService userService;

    public DocumentController(DocumentService documentService,
                              DocumentIntelligenceService intelligenceService,
                              UserService userService) {
        this.documentService = documentService;
        this.intelligenceService = intelligenceService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "parentDocumentId", required = false) Long parentDocumentId,
            @RequestParam(value = "allowDuplicate", required = false, defaultValue = "false") Boolean allowDuplicate
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentResponse response = documentService.uploadDocument(user, file, folderId, parentDocumentId, allowDuplicate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/check-duplicate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DuplicateCheckResponse> checkDuplicate(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        User user = userService.getByEmail(authentication.getName());
        DuplicateCheckResponse response = documentService.checkDuplicate(user, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/intelligence")
    public ResponseEntity<DocumentIntelligenceResponse> getIntelligence(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(intelligenceService.generateOrGetDocumentIntelligence(user, id));
    }

    @PostMapping("/{id}/regenerate-intelligence")
    public ResponseEntity<DocumentIntelligenceResponse> regenerateIntelligence(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(intelligenceService.generateOrGetDocumentIntelligence(user, id));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listDocuments(
            Authentication authentication,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) DocumentFormat format,
            @RequestParam(required = false) String search
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DocumentResponse> documents = documentService.listDocuments(user, status, format, search);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentResponse document = documentService.getDocument(id, user);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<DocumentStatusResponse> getDocumentStatus(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentStatusResponse status = documentService.getDocumentStatus(id, user);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<DocumentContentResponse> getDocumentContent(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentContentResponse content = documentService.getDocumentContent(id, user);
        return ResponseEntity.ok(content);
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<DocumentChunkResponse>> getDocumentChunks(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        List<DocumentChunkResponse> chunks = documentService.getDocumentChunks(id, user);
        return ResponseEntity.ok(chunks);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentService.DownloadFileInfo fileInfo = documentService.downloadDocument(id, user);

        String encodedFilename = URLEncoder.encode(fileInfo.getOriginalFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"; filename*=UTF-8''" + encodedFilename)
                .body(fileInfo.getResource());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<DocumentResponse> retryProcessing(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        DocumentResponse response = documentService.retryProcessing(id, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        documentService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }
}

