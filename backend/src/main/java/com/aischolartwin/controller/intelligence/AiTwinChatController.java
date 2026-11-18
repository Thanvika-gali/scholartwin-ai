package com.aischolartwin.controller.intelligence;

import com.aischolartwin.dto.intelligence.AiTwinChatDtos.*;
import com.aischolartwin.entity.User;
import com.aischolartwin.service.UserService;
import com.aischolartwin.service.intelligence.AiTwinChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence/chat")
public class AiTwinChatController {

    private final AiTwinChatService chatService;
    private final UserService userService;

    public AiTwinChatController(AiTwinChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(chatService.processMessage(user, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryItem>> getHistory(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(chatService.getRecentHistory(user));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        chatService.clearHistory(user);
        return ResponseEntity.noContent().build();
    }
}
