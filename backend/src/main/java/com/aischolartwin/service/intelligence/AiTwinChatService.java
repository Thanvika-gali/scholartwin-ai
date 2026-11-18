package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.AiTwinChatDtos.*;
import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.KnowledgeItemRepository;
import com.aischolartwin.repository.intelligence.AIInteractionLogRepository;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import com.aischolartwin.repository.learning.LearningProfileRepository;
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
public class AiTwinChatService {

    private static final Logger log = LoggerFactory.getLogger(AiTwinChatService.class);

    private final MemoryService memoryService;
    private final MemoryPreferenceRepository preferenceRepository;
    private final LearningProfileRepository profileRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final AIInteractionLogRepository interactionLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model.name:gemini-2.5-flash}")
    private String geminiModel;

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s+prompt|disregard\\s+rules|dan\\s+mode|jailbreak|reveal\\s+secret)",
            Pattern.CASE_INSENSITIVE
    );

    public AiTwinChatService(MemoryService memoryService,
                            MemoryPreferenceRepository preferenceRepository,
                            LearningProfileRepository profileRepository,
                            KnowledgeItemRepository knowledgeItemRepository,
                            AIInteractionLogRepository interactionLogRepository,
                            ObjectMapper objectMapper) {
        this.memoryService = memoryService;
        this.preferenceRepository = preferenceRepository;
        this.profileRepository = profileRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.interactionLogRepository = interactionLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatMessageResponse processMessage(User user, ChatMessageRequest request) {
        long startTime = System.currentTimeMillis();
        String rawQuery = request.effectiveMessage();
        String sanitizedQuery = sanitizePrompt(rawQuery);

        // 1. Determine Intent
        String intent = detectIntent(sanitizedQuery);

        // 2. Retrieve Accessible Memories Grounded in Preferences
        List<UserMemory> accessibleMemories = memoryService.getAccessibleMemoriesForUser(user, request.includeContextCategories());
        List<MemoryContextItem> usedMemories = new ArrayList<>();
        List<Long> usedMemoryIds = new ArrayList<>();

        StringBuilder memoryContextBuilder = new StringBuilder();
        if (!accessibleMemories.isEmpty()) {
            memoryContextBuilder.append("--- RETRIEVED STUDENT MEMORY ---\n");
            for (UserMemory m : accessibleMemories.stream().limit(8).toList()) {
                usedMemories.add(new MemoryContextItem(m.getId(), m.getCategory(), m.getSummary(), m.getContent()));
                usedMemoryIds.add(m.getId());
                memoryContextBuilder.append("[").append(m.getCategory()).append("] ").append(m.getContent()).append("\n");
            }
        }

        // 3. Retrieve Relevant Knowledge Vault Notes
        StringBuilder vaultContextBuilder = new StringBuilder();
        try {
            List<KnowledgeItem> notes = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user);
            List<String> matchingTitles = new ArrayList<>();
            for (KnowledgeItem n : notes) {
                if (n.getTitle() != null && sanitizedQuery.toLowerCase().contains(n.getTitle().toLowerCase())) {
                    matchingTitles.add(n.getTitle());
                }
            }
            if (!matchingTitles.isEmpty()) {
                vaultContextBuilder.append("--- VAULT REFERENCES ---\n");
                for (String t : matchingTitles.stream().limit(3).toList()) {
                    vaultContextBuilder.append("Knowledge Item: ").append(t).append("\n");
                }
            }
        } catch (Exception ignored) {}

        // 4. Student Role & Background
        String targetRole = profileRepository.findByUser(user)
                .map(LearningProfile::getTargetRole)
                .orElse("Software Development Engineer");

        // 5. Check for Gemini Key
        String apiKey = (geminiApiKey != null && !geminiApiKey.isBlank()) ? geminiApiKey : System.getenv("GEMINI_API_KEY");
        String responseText;
        String groundingMode;

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                responseText = callGemini(apiKey, sanitizedQuery, targetRole, memoryContextBuilder.toString(), vaultContextBuilder.toString());
                groundingMode = "GEMINI_RESPONSE";
            } catch (Exception e) {
                log.warn("Gemini call failed, falling back to rule-based engine: {}", e.getMessage());
                if (isContextRequiringQuery(intent) && accessibleMemories.isEmpty()) {
                    responseText = "### [Insufficient Context Available]\n\n" +
                            "I cannot diagnose your " + intent.toLowerCase().replace('_', ' ') + " because no verified activity records exist in your profile yet. " +
                            "Please complete assessments, log study sessions, or add explicit memories in the Memory Center.";
                    groundingMode = "INSUFFICIENT_CONTEXT";
                } else {
                    responseText = "[Notice: Gemini API is temporarily unavailable; responding using verified local profile records]\n\n" +
                            generateGroundedFallback(sanitizedQuery, intent, targetRole, accessibleMemories);
                    groundingMode = "PROVIDER_UNAVAILABLE";
                }
            }
        } else {
            if (isContextRequiringQuery(intent) && accessibleMemories.isEmpty()) {
                responseText = "### [Insufficient Context Available]\n\n" +
                        "I cannot diagnose your " + intent.toLowerCase().replace('_', ' ') + " because no verified activity records exist in your profile yet. " +
                        "Please complete assessments, log study sessions, or add explicit memories in the Memory Center.";
                groundingMode = "INSUFFICIENT_CONTEXT";
            } else {
                responseText = generateGroundedFallback(sanitizedQuery, intent, targetRole, accessibleMemories);
                groundingMode = "RULE_BASED_FALLBACK";
            }
        }

        long latency = System.currentTimeMillis() - startTime;

        // 6. Automatic Candidate Memory Detection from User Message
        int candidateMemoriesCount = 0;
        MemoryPreference pref = preferenceRepository.findByUser(user).orElse(null);
        if (pref != null && Boolean.TRUE.equals(pref.getMemoryEnabled()) && Boolean.TRUE.equals(pref.getAutoExtractFromChat())) {
            candidateMemoriesCount = extractChatCandidateMemory(user, sanitizedQuery);
        }

        // 7. Log Interaction
        try {
            String memoryIdsJson = objectMapper.writeValueAsString(usedMemoryIds);
            AIInteractionLog logEntry = new AIInteractionLog(
                    user,
                    sanitizedQuery,
                    responseText,
                    intent,
                    memoryIdsJson,
                    "[]",
                    groundingMode,
                    candidateMemoriesCount,
                    latency
            );
            interactionLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to log interaction: {}", e.getMessage());
        }

        List<String> followUps = generateFollowUps(intent, targetRole);

        return new ChatMessageResponse(
                responseText,
                responseText,
                intent,
                groundingMode,
                usedMemories,
                usedMemories,
                followUps,
                candidateMemoriesCount,
                latency
        );
    }

    @Transactional(readOnly = true)
    public List<ChatHistoryItem> getRecentHistory(User user) {
        return interactionLogRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .limit(20)
                .map(l -> new ChatHistoryItem(
                        l.getId(),
                        l.getQuery(),
                        l.getResponse(),
                        l.getIntent(),
                        l.getGroundingMode(),
                        l.getCreatedAt().toString()
                ))
                .toList();
    }

    @Transactional
    public void clearHistory(User user) {
        interactionLogRepository.deleteByUser(user);
    }

    private String sanitizePrompt(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.length() > 2500) {
            s = s.substring(0, 2500);
        }
        if (INJECTION_PATTERN.matcher(s).find()) {
            s = s.replaceAll("(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s+prompt|disregard\\s+rules|dan\\s+mode|jailbreak)", "[FILTERED]");
        }
        return s;
    }

    private String detectIntent(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("weak") || lower.contains("gap") || lower.contains("struggle") || lower.contains("difficulty")) {
            return "WEAKNESS_QUERY";
        } else if (lower.contains("learn next") || lower.contains("roadmap") || lower.contains("what should i")) {
            return "LEARNING_ADVICE";
        } else if (lower.contains("role") || lower.contains("job") || lower.contains("career") || lower.contains("resume") || lower.contains("prepared")) {
            return "CAREER_READINESS";
        } else if (lower.contains("revise") || lower.contains("review") || lower.contains("flashcard") || lower.contains("forget")) {
            return "REVISION_QUERY";
        } else if (lower.contains("decision") || lower.contains("previous") || lower.contains("project")) {
            return "PROJECT_RECALL";
        } else {
            return "CONCEPT_EXPLANATION";
        }
    }

    private String callGemini(String apiKey, String query, String targetRole, String memoryCtx, String vaultCtx) throws Exception {
        String prompt = """
                You are AI ScholarTwin, the student's personalized academic twin and engineering mentor.
                Student Target Role: %s
                
                %s
                
                %s
                
                Student Question:
                %s
                
                Instructions:
                1. Always prioritize and cite the student's stored memories and actual academic records above.
                2. If the user asks about their weak areas, target role, or progress, use the exact facts from their memory.
                3. When information is missing, explicitly state that no record exists in their profile rather than hallucinating.
                4. Maintain an encouraging, precise, technical tone with clean markdown formatting.
                """.formatted(targetRole, memoryCtx, vaultCtx, query);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        String jsonBody = objectMapper.writeValueAsString(body);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(20))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode candidate = root.path("candidates").get(0);
            return candidate.path("content").path("parts").get(0).path("text").asText();
        } else {
            throw new RuntimeException("Gemini returned HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private boolean isContextRequiringQuery(String intent) {
        return "WEAKNESS_QUERY".equals(intent) || "CAREER_READINESS".equals(intent) || "REVISION_QUERY".equals(intent);
    }

    private String generateGroundedFallback(String query, String intent, String targetRole, List<UserMemory> memories) {
        StringBuilder sb = new StringBuilder();
        sb.append("### [Rule-Based Fallback Response]\n\n");

        if ("WEAKNESS_QUERY".equals(intent)) {
            List<UserMemory> weaknesses = memories.stream().filter(m -> m.getMemoryType() == MemoryType.WEAKNESS).toList();
            if (!weaknesses.isEmpty()) {
                sb.append("Based on your **Personalized Learning Memory**, here are your currently diagnosed weak areas:\n\n");
                for (UserMemory w : weaknesses) {
                    sb.append("- **").append(w.getSummary()).append("**: ").append(w.getContent()).append("\n");
                }
                sb.append("\n**Recommended Next Action:** Schedule active recall flashcards in the **Spaced Revision Engine** to reinforce these topics before your next assessment.");
            } else {
                sb.append("You currently have no critical weakness memories recorded! Keep tackling new medium-difficulty problem sets to uncover edge-case gaps.");
            }
        } else if ("CAREER_READINESS".equals(intent)) {
            sb.append("Your current profile is optimized for the **").append(targetRole).append("** engineering benchmark.\n\n");
            List<UserMemory> tech = memories.stream().filter(m -> m.getCategory() == MemoryCategory.TECHNICAL).toList();
            if (!tech.isEmpty()) {
                sb.append("**Verified Active Skills:**\n");
                for (UserMemory t : tech.stream().limit(5).toList()) {
                    sb.append("- ").append(t.getContent()).append("\n");
                }
            }
            sb.append("\n**Preparation Strategy:** Ensure your resume bullet points quantify system throughput and latency improvements.");
        } else if ("LEARNING_ADVICE".equals(intent)) {
            sb.append("To advance towards your **").append(targetRole).append("** milestones, focus on:\n\n");
            sb.append("1. **Complete scheduled Roadmap tasks** in your daily planner.\n");
            sb.append("2. **Reinforce active recall** on topics scheduled for today in Spaced Revision.\n");
            sb.append("3. **Practice algorithmic patterns** in trees, graphs, and two-pointer convergence.\n");
        } else {
            sb.append("Based on your stored context for **").append(targetRole).append("**:\n\n");
            sb.append(query).append(" is a critical topic in modern systems engineering. Focus on invariant guarantees, time/space complexity tradeoffs, and thread-safety under concurrent load.\n\n");
            if (!memories.isEmpty()) {
                sb.append("*Grounded in ").append(memories.size()).append(" active profile memories across your learning vault.*");
            }
        }

        return sb.toString();
    }

    private int extractChatCandidateMemory(User user, String query) {
        String lower = query.toLowerCase();
        if (lower.startsWith("i want to become ") || lower.startsWith("my target role is ") || lower.contains("my goal is ")) {
            String extracted = query.replaceAll("(?i)(i want to become|my target role is|my goal is)\\s+", "").trim();
            if (extracted.length() > 3 && extracted.length() < 100) {
                memoryService.createMemory(user, new MemoryDtos.CreateMemoryRequest(
                        MemoryCategory.CAREER,
                        "Career Aspiration",
                        "User explicitly shared: " + query,
                        MemoryType.GOAL,
                        MemoryProvenance.AI_EXTRACTED,
                        MemorySourceType.CHAT,
                        "AI Twin Chat",
                        0.9,
                        MemoryImportance.HIGH
                ));
                return 1;
            }
        }
        return 0;
    }

    private List<String> generateFollowUps(String intent, String targetRole) {
        return switch (intent) {
            case "WEAKNESS_QUERY" -> List.of(
                    "What study plan addresses these weak concepts?",
                    "Generate a diagnostic quiz on my weak areas",
                    "Which DSA patterns need more practice?"
            );
            case "CAREER_READINESS" -> List.of(
                    "What missing skills are needed for " + targetRole + "?",
                    "How can I improve my resume ATS match?",
                    "What interview topics are tested most for " + targetRole + "?"
            );
            case "LEARNING_ADVICE" -> List.of(
                    "Show my daily study capacity today",
                    "Which roadmap phase is currently active?",
                    "What revisions are due today?"
            );
            default -> List.of(
                    "What are the time and space complexity tradeoffs?",
                    "Show code implementation best practices",
                    "What diagnostic questions test this concept?"
            );
        };
    }
}
