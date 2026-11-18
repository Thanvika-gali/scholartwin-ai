package com.aischolartwin.controller.learning;

import com.aischolartwin.dto.learning.AiLearningAssistantDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.learning.AiLearningAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/learning/assistant")
public class AiLearningAssistantController {

    private final AiLearningAssistantService assistantService;
    private final UserService userService;

    public AiLearningAssistantController(AiLearningAssistantService assistantService, UserService userService) {
        this.assistantService = assistantService;
        this.userService = userService;
    }

    @PostMapping("/query")
    public ResponseEntity<AiLearningAssistantDtos.Response> consultAssistant(
            Authentication authentication,
            @Valid @RequestBody AiLearningAssistantDtos.Request request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(assistantService.consultAssistant(user, request));
    }
}
