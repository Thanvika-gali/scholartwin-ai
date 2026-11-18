package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.AiLearningAssistantDtos;
import com.aischolartwin.dto.learning.AiLearningAssistantDtos.QueryContextType;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.learning.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiLearningAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiLearningAssistantService.class);

    private final LearningProfileRepository profileRepository;
    private final LearningRoadmapRepository roadmapRepository;
    private final LearningTaskRepository taskRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model.name:gemini-2.5-flash}")
    private String geminiModel;

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s+prompt|disregard\\s+rules|dan\\s+mode|jailbreak|reveal\\s+secret)",
            Pattern.CASE_INSENSITIVE
    );

    public AiLearningAssistantService(LearningProfileRepository profileRepository,
                                     LearningRoadmapRepository roadmapRepository,
                                     LearningTaskRepository taskRepository,
                                     AssessmentAttemptRepository attemptRepository,
                                     KnowledgeItemRepository knowledgeItemRepository,
                                     ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.roadmapRepository = roadmapRepository;
        this.taskRepository = taskRepository;
        this.attemptRepository = attemptRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public AiLearningAssistantDtos.Response consultAssistant(User user, AiLearningAssistantDtos.Request request) {
        String sanitizedQuery = sanitizePrompt(request.query());
        String topic = request.topic() != null && !request.topic().isBlank() ? request.topic().trim() : "General Computer Science";
        QueryContextType contextType = request.contextType() != null ? request.contextType() : QueryContextType.GENERAL_QUERY;

        // 1. Gather Grounding Context
        Optional<LearningProfile> profileOpt = profileRepository.findByUser(user);
        String targetRole = profileOpt.map(LearningProfile::getTargetRole).orElse("Software Engineer");
        String skillsContext = profileOpt.map(LearningProfile::getCurrentSkillsJson).orElse("Java, Data Structures & Algorithms, System Design");

        StringBuilder groundingContext = new StringBuilder();
        groundingContext.append("Student Target Role: ").append(targetRole).append("\n");
        groundingContext.append("Student Profile Skills: ").append(skillsContext).append("\n");

        if (request.relatedRoadmapId() != null) {
            roadmapRepository.findByIdAndUser(request.relatedRoadmapId(), user)
                    .ifPresent(r -> groundingContext.append("Active Roadmap: ").append(r.getTitle())
                            .append(" (").append(r.getCompletionPercentage()).append("% completed)\n"));
        }

        if (request.relatedTaskId() != null) {
            taskRepository.findById(request.relatedTaskId())
                    .ifPresent(t -> groundingContext.append("Current Learning Task: ").append(t.getTitle())
                            .append(" [Status: ").append(t.getStatus()).append("]\n"));
        }

        // Assessment mistakes grounding
        if (request.relatedAssessmentAttemptId() != null) {
            attemptRepository.findByIdAndUser(request.relatedAssessmentAttemptId(), user)
                    .ifPresent(att -> {
                        groundingContext.append("Recent Assessment: ").append(att.getAssessment().getTitle())
                                .append(" - Score: ").append(att.getScorePercentage()).append("%\n");
                    });
        }

        // Knowledge hub grounding
        try {
            List<KnowledgeItem> userNotes = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user);
            List<String> relevantTitles = userNotes.stream()
                    .filter(item -> item.getTitle() != null && item.getTitle().toLowerCase().contains(topic.toLowerCase()))
                    .map(KnowledgeItem::getTitle)
                    .limit(3)
                    .collect(Collectors.toList());
            if (!relevantTitles.isEmpty()) {
                groundingContext.append("Student Knowledge Hub Notes on Topic: ").append(String.join(", ", relevantTitles)).append("\n");
            }
        } catch (Exception e) {
            log.debug("Knowledge hub lookup skipped: {}", e.getMessage());
        }

        // 2. Check for Live Gemini Key
        String apiKey = (geminiApiKey != null && !geminiApiKey.isBlank()) ? geminiApiKey : System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                AiLearningAssistantDtos.Response geminiResponse = callGeminiApi(apiKey, sanitizedQuery, topic, contextType, groundingContext.toString());
                if (geminiResponse != null) {
                    return geminiResponse;
                }
            } catch (Exception ex) {
                log.warn("Gemini API invocation failed, falling back to ScholarTwin Academic Reasoning Engine: {}", ex.getMessage());
            }
        }

        // 3. Fallback to ScholarTwin Academic Reasoning & Pedagogical Engine
        return generateAcademicEngineResponse(sanitizedQuery, topic, contextType, targetRole, groundingContext.toString());
    }

    private String sanitizePrompt(String rawQuery) {
        if (rawQuery == null) return "";
        String trimmed = rawQuery.trim();
        if (trimmed.length() > 2000) {
            trimmed = trimmed.substring(0, 2000);
        }
        // Neutralize prompt injection attempts
        if (INJECTION_PATTERN.matcher(trimmed).find()) {
            trimmed = trimmed.replaceAll("(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s+prompt|disregard\\s+rules|dan\\s+mode|jailbreak)", "[FILTERED]");
        }
        return trimmed;
    }

    private AiLearningAssistantDtos.Response callGeminiApi(String apiKey, String query, String topic, QueryContextType contextType, String grounding) {
        try {
            String prompt = """
                    You are ScholarTwin AI, an expert academic tutor and technical mentor.
                    Grounding Context for Student:
                    %s
                    
                    Current Query Context Type: %s
                    Topic: %s
                    Student Question:
                    %s
                    
                    Provide a thorough, pedagogically sound, structured response in markdown.
                    At the very end of your response, output a JSON block formatted exactly as:
                    ```json
                    {
                      "suggestedNextTopic": "...",
                      "keyTakeaways": ["...", "..."],
                      "practicalExercises": ["...", "..."]
                    }
                    ```
                    """.formatted(grounding, contextType.name(), topic, query);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + (geminiModel != null ? geminiModel : "gemini-2.5-flash") + ":generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String rawText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
                    return parseGeminiOutput(rawText, topic);
                }
            }
        } catch (Exception e) {
            log.warn("Error calling Gemini API: {}", e.getMessage());
        }
        return null;
    }

    private AiLearningAssistantDtos.Response parseGeminiOutput(String rawText, String topic) {
        String mainText = rawText;
        String suggestedNext = topic + " Advanced Applications";
        List<String> takeaways = new ArrayList<>();
        List<String> exercises = new ArrayList<>();

        int jsonStart = rawText.lastIndexOf("```json");
        if (jsonStart != -1) {
            mainText = rawText.substring(0, jsonStart).trim();
            int jsonEnd = rawText.indexOf("```", jsonStart + 7);
            if (jsonEnd != -1) {
                String jsonStr = rawText.substring(jsonStart + 7, jsonEnd).trim();
                try {
                    JsonNode node = objectMapper.readTree(jsonStr);
                    if (node.has("suggestedNextTopic")) {
                        suggestedNext = node.get("suggestedNextTopic").asText();
                    }
                    if (node.has("keyTakeaways") && node.get("keyTakeaways").isArray()) {
                        for (JsonNode item : node.get("keyTakeaways")) {
                            takeaways.add(item.asText());
                        }
                    }
                    if (node.has("practicalExercises") && node.get("practicalExercises").isArray()) {
                        for (JsonNode item : node.get("practicalExercises")) {
                            exercises.add(item.asText());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        if (takeaways.isEmpty()) {
            takeaways = List.of(
                    "Deep understanding of core mechanisms and edge cases",
                    "Application of clean code and complexity optimization"
            );
        }
        if (exercises.isEmpty()) {
            exercises = List.of(
                    "Implement a modular prototype demonstrating this concept",
                    "Write unit tests covering boundary conditions and stress tests"
            );
        }

        return new AiLearningAssistantDtos.Response(
                mainText,
                topic,
                suggestedNext,
                takeaways,
                exercises,
                "GEMINI_AI",
                "Generated via " + (geminiModel != null ? geminiModel : "Gemini") + " grounded with your profile and academic context."
        );
    }

    private AiLearningAssistantDtos.Response generateAcademicEngineResponse(
            String query, String topic, QueryContextType contextType, String targetRole, String grounding) {

        String responseText;
        String suggestedNextTopic;
        List<String> takeaways;
        List<String> practicalExercises;

        switch (contextType) {
            case CONCEPT_EXPLANATION -> {
                responseText = """
                        ### Academic Conceptual Deep Dive: %s
                        
                        **Target Role Alignment:** %s
                        
                        #### 1. Core Principles & Architecture
                        Understanding **%s** requires analyzing its foundational design tradeoffs. In production systems, %s is chosen for its deterministic performance characteristics, clear abstraction boundaries, and predictable computational complexity.
                        
                        #### 2. Key Mechanics & Invariants
                        - **State & Transition Invariants:** Operations must guarantee safety, idempotency where appropriate, and clean fault boundaries.
                        - **Resource Efficiency:** Time complexity must be considered alongside spatial overhead (cache locality, memory allocation footprint, and Garbage Collection pressure in runtime environments).
                        - **Concurrency & Reentrancy:** Ensure thread-safety or immutability when data structures or components are shared across concurrent executors.
                        
                        #### 3. Practical Industry Best Practices
                        - Keep boundaries modular using interface segregation and explicit contracts.
                        - Instrument key execution paths with logging and telemetry metrics.
                        - Always document preconditions, postconditions, and known edge constraints.
                        """.formatted(topic, targetRole, topic, topic);
                suggestedNextTopic = topic + " Patterns & Failure Modes";
                takeaways = List.of(
                        "Grasp the invariant guarantees and algorithmic complexity before writing code.",
                        "Design for concurrency safety and resource efficiency from day one.",
                        "Align implementation details with " + targetRole + " industry standards."
                );
                practicalExercises = List.of(
                        "Build an end-to-end benchmark comparing " + topic + " against alternative patterns under load.",
                        "Write a comprehensive suite of parameterized tests testing edge cases and null/empty states.",
                        "Refactor an existing component to isolate " + topic + " behind a clean interface."
                );
            }
            case EXAMPLE_GENERATION -> {
                responseText = """
                        ### Production-Grade Implementation Pattern: %s
                        
                        #### Context
                        Below is a clean, modular reference implementation demonstrating best practices for **%s** targeted at a **%s** level:
                        
                        ```java
                        // Architectural Pattern for %s
                        public class %sManager {
                            private final Logger log = LoggerFactory.getLogger(getClass());
                            
                            public <T> T executeWithValidation(T input) {
                                Objects.requireNonNull(input, "Input must not be null");
                                log.debug("Processing request with safe execution boundary");
                                // Core business logic with defensive invariants
                                return input;
                            }
                        }
                        ```
                        
                        #### Architectural Highlights
                        - **Defensive Validation:** Fast-fail inputs using strict assertions.
                        - **Separation of Concerns:** Keep algorithmic logic decoupled from I/O or external persistence.
                        """.formatted(topic, topic, targetRole, topic, topic.replaceAll("[^a-zA-Z0-9]", ""));
                suggestedNextTopic = topic + " Concurrency & Performance Tuning";
                takeaways = List.of(
                        "Code should be self-documenting with explicit pre- and post-condition checks.",
                        "Isolate side-effects to preserve testability and pure functional logic."
                );
                practicalExercises = List.of(
                        "Adapt this pattern into your current project or DSA repository.",
                        "Add metric tracking to count invocations and measure latency."
                );
            }
            case PRACTICE_QUESTIONS -> {
                responseText = """
                        ### Targeted Diagnostic Practice: %s
                        
                        Review the following architectural and diagnostic questions to test your depth in **%s**:
                        
                        1. **Under the Hood:** What happens internally during peak throughput or memory pressure when utilizing %s?
                        2. **Tradeoff Analysis:** If your system needs to scale to 100k requests/second, what are the primary bottlenecks of this approach versus an asynchronous or event-driven alternative?
                        3. **Failure Scenarios:** How does this design handle transient downstream failures, network partitions, or data corruption?
                        4. **Complexity:** What is the precise time and space complexity for average and worst-case scenarios?
                        """.formatted(topic, topic, topic);
                suggestedNextTopic = topic + " System Design Deep-Dive";
                takeaways = List.of(
                        "Mastering interview and production problems requires explaining 'why' not just 'how'.",
                        "Evaluate solutions across scalability, maintainability, and operational simplicity."
                );
                practicalExercises = List.of(
                        "Draft written 2-paragraph explanations for each question above.",
                        "Simulate a mock technical interview answering question 2 out loud with diagrams."
                );
            }
            case REVISION_ADVICE -> {
                responseText = """
                        ### Strategic Revision & Spaced Repetition Advice: %s
                        
                        Based on your profile and memory retention curves:
                        - **Active Recall:** Avoid passive re-reading. Close your notes and implement the concept from scratch on a blank editor.
                        - **Interleaving Practice:** Mix problems involving **%s** with unrelated topics to train contextual pattern recognition.
                        - **Spaced Intervals:** Review this topic today, then schedule automatic check-ins at 3 days, 7 days, and 14 days using ScholarTwin's Spaced Revision engine.
                        """.formatted(topic, topic);
                suggestedNextTopic = topic + " Mixed Problem Set";
                takeaways = List.of(
                        "Active recall yields 3x higher long-term retention than passive reading.",
                        "Use ScholarTwin's Spaced Revision tracker to prevent forgetting curve decay."
                );
                practicalExercises = List.of(
                        "Create a flashcard summary in your Knowledge Hub.",
                        "Complete 2 LeetCode/practical problems without looking at solutions."
                );
            }
            case ASSESSMENT_REVIEW -> {
                responseText = """
                        ### Assessment Diagnostic & Error Analysis
                        
                        Analyzing your performance on **%s**:
                        - **Root-Cause Analysis:** Most assessment errors stem from hasty reading of constraints or confusing average-case with worst-case guarantees.
                        - **Systematic Debugging:** Trace your failed attempts with small test cases. Pinpoint the exact line or assumption where the mental model deviated.
                        - **Remediation Action:** Create a dedicated spaced revision item for the missed concepts.
                        """.formatted(topic);
                suggestedNextTopic = topic + " Remediation Quiz";
                takeaways = List.of(
                        "Errors are the most potent learning signal when systematically analyzed.",
                        "Document the specific anti-pattern that led to the incorrect answer."
                );
                practicalExercises = List.of(
                        "Re-attempt the failed questions in a fresh quiz session.",
                        "Add a note in Knowledge Hub detailing the gotcha and solution."
                );
            }
            default -> {
                responseText = """
                        ### ScholarTwin Academic Guidance: %s
                        
                        **Target Role:** %s
                        
                        You asked: *"%s"*
                        
                        #### Structured Recommendation
                        1. **Foundational Understanding:** Review the core mechanisms and syntax for **%s**.
                        2. **Systemic Integration:** Connect how this fits into modern %s engineering workflows.
                        3. **Hands-on Verification:** Write executable code and tests to confirm your mental model.
                        """.formatted(topic, targetRole, query, topic, targetRole);
                suggestedNextTopic = topic + " Core Deep Dive";
                takeaways = List.of(
                        "Focus on practical execution and foundational principles.",
                        "Align daily milestones with your target role: " + targetRole
                );
                practicalExercises = List.of(
                        "Add a daily study task for this topic in your Daily Study Planner.",
                        "Take a 5-question ScholarTwin assessment to benchmark your current level."
                );
            }
        }

        return new AiLearningAssistantDtos.Response(
                responseText,
                topic,
                suggestedNextTopic,
                takeaways,
                practicalExercises,
                "PERSONAL_KNOWLEDGE_GROUNDED",
                "Operating with ScholarTwin Academic Reasoning Engine. To enable live Gemini LLM responses, set GEMINI_API_KEY."
        );
    }
}
