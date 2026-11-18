package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.MemoryCategory;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AiTwinChatDtos {

    public record ChatMessageRequest(
            String message,
            String prompt,
            String conversationTopic,
            List<MemoryCategory> includeContextCategories
    ) {
        public ChatMessageRequest(String message, String conversationTopic, List<MemoryCategory> includeContextCategories) {
            this(message, null, conversationTopic, includeContextCategories);
        }

        public String effectiveMessage() {
            if (message != null && !message.isBlank()) return message.trim();
            if (prompt != null && !prompt.isBlank()) return prompt.trim();
            return "";
        }
    }

    public record MemoryContextItem(
            Long memoryId,
            MemoryCategory category,
            String summary,
            String content
    ) {}

    public record ChatMessageResponse(
            String response,
            String reply,
            String intent,
            String groundingMode,
            List<MemoryContextItem> usedMemories,
            List<MemoryContextItem> groundedMemories,
            List<String> suggestedFollowUps,
            int candidateMemoriesIdentifiedCount,
            long latencyMs
    ) {}

    public record ChatHistoryItem(
            Long id,
            String query,
            String response,
            String intent,
            String groundingMode,
            String createdAt
    ) {}
}
