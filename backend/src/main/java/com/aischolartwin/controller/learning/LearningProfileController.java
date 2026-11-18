package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.LearningProfileDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.LearningProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learning/profile")
public class LearningProfileController {

    private final LearningProfileService profileService;
    private final UserService userService;

    public LearningProfileController(LearningProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<LearningProfileDto.Response> getProfile(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<LearningProfileDto.Response> updateProfile(
            Authentication authentication,
            @Valid @RequestBody LearningProfileDto.Request request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }
}
