package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.MemoryPreferenceDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.MemoryPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intelligence/memory-preferences")
public class MemoryPreferenceController {

    private final MemoryPreferenceService preferenceService;
    private final UserService userService;

    public MemoryPreferenceController(MemoryPreferenceService preferenceService, UserService userService) {
        this.preferenceService = preferenceService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<MemoryPreferenceDto.Response> getPreferences(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(preferenceService.getOrCreatePreferences(user));
    }

    @PutMapping
    public ResponseEntity<MemoryPreferenceDto.Response> updatePreferences(
            Authentication authentication,
            @RequestBody MemoryPreferenceDto.Request request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(preferenceService.updatePreferences(user, request));
    }
}
