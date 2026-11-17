package com.aischolartwin.controller;

import com.aischolartwin.dto.*;
import com.aischolartwin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller managing authenticated user profile and settings operations.
 * All endpoints derive the target identity strictly from the authenticated server-side principal.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile and settings of the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates permitted profile information (legacy name update) for the authenticated user.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Comprehensive profile information update.
     */
    @PutMapping("/me/profile")
    public ResponseEntity<UserResponse> updateFullProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserResponse response = userService.updateFullProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates academic preferences.
     */
    @PutMapping("/me/academic-preferences")
    public ResponseEntity<UserResponse> updateAcademicPreferences(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody AcademicPreferencesDto dto
    ) {
        UserResponse response = userService.updateAcademicPreferences(userDetails.getUsername(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates AI preferences.
     */
    @PutMapping("/me/ai-preferences")
    public ResponseEntity<UserResponse> updateAiPreferences(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody AiPreferencesDto dto
    ) {
        UserResponse response = userService.updateAiPreferences(userDetails.getUsername(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates notification preferences.
     */
    @PutMapping("/me/notification-preferences")
    public ResponseEntity<UserResponse> updateNotificationPreferences(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody NotificationPreferencesDto dto
    ) {
        UserResponse response = userService.updateNotificationPreferences(userDetails.getUsername(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates theme appearance preference.
     */
    @PutMapping("/me/appearance")
    public ResponseEntity<UserResponse> updateThemePreference(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody Map<String, String> body
    ) {
        String theme = body != null ? body.get("theme") : "SYSTEM";
        UserResponse response = userService.updateThemePreference(userDetails.getUsername(), theme);
        return ResponseEntity.ok(response);
    }

    /**
     * Changes user account password with old password verification.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    /**
     * Exports all user data across all ScholarTwin modules in a single JSON document.
     */
    @GetMapping("/me/export-data")
    public ResponseEntity<DataExportResponse> exportData(@AuthenticationPrincipal UserDetails userDetails) {
        DataExportResponse response = userService.exportUserData(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes user account and cascades across all documents, resumes, and data.
     */
    @DeleteMapping("/me/account")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Account and all associated personal data have been permanently deleted."));
    }
}
