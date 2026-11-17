package com.aischolartwin.controller;

import com.aischolartwin.dto.ResumeDetailResponse;
import com.aischolartwin.dto.ResumeResponse;
import com.aischolartwin.dto.ResumeUpdateRequest;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.ResumeService;
import com.aischolartwin.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/career/resumes")
public class CareerResumeController {

    private final ResumeService resumeService;
    private final UserService userService;

    public CareerResumeController(ResumeService resumeService, UserService userService) {
        this.resumeService = resumeService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeDetailResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeDetailResponse response = resumeService.uploadResume(user, file, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> listResumes(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        List<ResumeResponse> list = resumeService.getUserResumes(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDetailResponse> getResumeDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeDetailResponse detail = resumeService.getResumeDetail(user, id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        Resource resource = resumeService.downloadResume(user, id);
        var resume = resumeService.getResumeEntity(user, id);

        String mediaType = "pdf".equalsIgnoreCase(resume.getFileType().name())
                ? MediaType.APPLICATION_PDF_VALUE
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDetailResponse> updateResume(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ResumeUpdateRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeDetailResponse response = resumeService.updateResume(user, id, request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = {"/{id}/primary", "/{id}/set-primary"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<ResumeResponse> setPrimary(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        ResumeResponse response = resumeService.setPrimaryResume(user, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            Authentication authentication,
            @PathVariable Long id
    ) {
        User user = userService.getByEmail(authentication.getName());
        resumeService.deleteResume(user, id);
        return ResponseEntity.noContent().build();
    }
}

