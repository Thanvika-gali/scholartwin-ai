package com.aischolartwin.service;

import com.aischolartwin.dto.DocumentIntelligenceResponse;
import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentChunk;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DocumentChunkRepository;
import com.aischolartwin.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocumentIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIntelligenceService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ObjectMapper objectMapper;

    // Academic Subjects & Keyword signatures
    private static final Map<String, List<String>> SUBJECT_SIGNATURES = new LinkedHashMap<>();

    static {
        SUBJECT_SIGNATURES.put("Database Systems (DBMS)", List.of(
                "dbms", "database", "sql", "normalization", "1nf", "2nf", "3nf", "bcnf", "relational", "acid", "transaction", "query", "schema", "table", "index", "b-tree"));
        SUBJECT_SIGNATURES.put("Data Structures & Algorithms", List.of(
                "algorithm", "data structure", "binary tree", "graph", "sorting", "dynamic programming", "recursion", "complexity", "big o", "linked list", "array", "heap", "stack", "queue"));
        SUBJECT_SIGNATURES.put("Operating Systems", List.of(
                "operating system", "kernel", "process", "thread", "scheduling", "deadlock", "paging", "virtual memory", "semaphore", "mutex", "ipc", "file system"));
        SUBJECT_SIGNATURES.put("Computer Networks", List.of(
                "network", "tcp", "udp", "ip", "protocol", "osi", "routing", "packet", "dns", "http", "socket", "subnet", "bandwidth"));
        SUBJECT_SIGNATURES.put("Artificial Intelligence & Machine Learning", List.of(
                "neural network", "machine learning", "deep learning", "embedding", "model", "training", "transformer", "classification", "regression", "nlp", "vector", "clustering"));
        SUBJECT_SIGNATURES.put("Software Engineering & Architecture", List.of(
                "software engineering", "microservices", "spring boot", "react", "architecture", "design pattern", "api", "rest", "docker", "kubernetes", "ci/cd", "agile", "refactoring"));
    }

    public DocumentIntelligenceService(DocumentRepository documentRepository,
                                       DocumentChunkRepository chunkRepository,
                                       ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DocumentIntelligenceResponse generateOrGetDocumentIntelligence(User user, Long documentId) {
        Document document = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found (id: " + documentId + ")"));

        List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(document);
        if (chunks.isEmpty()) {
            return new DocumentIntelligenceResponse(
                    document.getId(),
                    document.getOriginalFilename(),
                    "No extractable text chunks found in document.",
                    List.of(), List.of(), List.of(), List.of(), List.of(),
                    "General Academic",
                    Map.of(),
                    Instant.now()
            );
        }

        StringBuilder fullText = new StringBuilder();
        Map<Integer, String> pageTextMap = new TreeMap<>();
        List<String> headings = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            String text = chunk.getChunkText() != null ? chunk.getChunkText() : "";
            fullText.append(text).append("\n");
            int page = chunk.getStartPage() > 0 ? chunk.getStartPage() : 1;
            pageTextMap.merge(page, text, (a, b) -> a + "\n" + b);
        }

        String contentStr = fullText.toString();

        // 1. Generate Summary (extractive summary from top relevant sentences)
        String summary = generateExtractiveSummary(contentStr);

        // 2. Identify Academic Subject
        String estimatedSubject = classifySubject(contentStr, document.getOriginalFilename());

        // 3. Extract Main Topics & Keywords
        List<String> mainTopics = extractTopics(contentStr);

        // 4. Extract Definitions
        List<String> keyDefinitions = extractDefinitions(contentStr);

        // 5. Extract Formulas & Formal expressions
        List<String> formulas = extractFormulas(contentStr);

        // 6. Suggested Tags
        List<String> suggestedTags = new ArrayList<>(mainTopics);
        if (!suggestedTags.contains(estimatedSubject)) {
            suggestedTags.add(0, estimatedSubject.split(" \\(")[0]);
        }

        // 7. Page References per topic
        Map<String, List<Integer>> conceptPageRefs = new LinkedHashMap<>();
        for (String topic : mainTopics.subList(0, Math.min(5, mainTopics.size()))) {
            List<Integer> pages = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : pageTextMap.entrySet()) {
                if (entry.getValue().toLowerCase().contains(topic.toLowerCase())) {
                    pages.add(entry.getKey());
                }
            }
            if (!pages.isEmpty()) {
                conceptPageRefs.put(topic, pages);
            }
        }

        // Persist to document entity
        document.setDocumentSummary(summary);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("mainTopics", mainTopics);
        metadata.put("keyDefinitions", keyDefinitions);
        metadata.put("formulas", formulas);
        metadata.put("keyHeadings", headings);
        metadata.put("suggestedTags", suggestedTags);
        metadata.put("estimatedSubject", estimatedSubject);
        metadata.put("conceptPageReferences", conceptPageRefs);

        try {
            document.setExtractedMetadataJson(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize document intelligence metadata JSON: {}", e.getMessage());
        }

        documentRepository.save(document);

        return new DocumentIntelligenceResponse(
                document.getId(),
                document.getOriginalFilename(),
                summary,
                mainTopics,
                keyDefinitions,
                formulas,
                headings,
                suggestedTags,
                estimatedSubject,
                conceptPageRefs,
                Instant.now()
        );
    }

    private String generateExtractiveSummary(String text) {
        String[] rawSentences = text.split("(?<=[.?!])\\s+");
        List<String> candidates = new ArrayList<>();

        for (String s : rawSentences) {
            String clean = s.trim().replaceAll("\\s+", " ");
            if (clean.length() >= 40 && clean.length() <= 240 && !clean.startsWith("http") && !clean.matches("^[0-9\\W]+$")) {
                candidates.add(clean);
            }
        }

        if (candidates.isEmpty()) {
            return text.length() > 300 ? text.substring(0, 300) + "..." : text;
        }

        // Pick 2-3 most representative sentences
        int pickCount = Math.min(3, candidates.size());
        return String.join(" ", candidates.subList(0, pickCount));
    }

    public String classifySubject(String content, String filename) {
        String combined = (filename + " " + content).toLowerCase();
        String bestSubject = "General Academic";
        int maxHits = 0;

        for (Map.Entry<String, List<String>> entry : SUBJECT_SIGNATURES.entrySet()) {
            int hits = 0;
            for (String kw : entry.getValue()) {
                if (combined.contains(kw)) {
                    hits++;
                }
            }
            if (hits > maxHits && hits >= 2) {
                maxHits = hits;
                bestSubject = entry.getKey();
            }
        }
        return bestSubject;
    }

    private List<String> extractTopics(String text) {
        Set<String> topics = new LinkedHashSet<>();
        String lower = text.toLowerCase();

        for (List<String> kws : SUBJECT_SIGNATURES.values()) {
            for (String kw : kws) {
                if (lower.contains(kw) && kw.length() > 2) {
                    // Capitalize title
                    String formatted = Arrays.stream(kw.split("\\s+"))
                            .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                            .collect(Collectors.joining(" "));
                    topics.add(formatted);
                }
            }
        }
        return topics.stream().limit(8).collect(Collectors.toList());
    }

    private List<String> extractDefinitions(String text) {
        List<String> definitions = new ArrayList<>();
        Pattern defPattern = Pattern.compile("(?i)\\b([A-Z][A-Za-z0-9\\s]{2,35})\\s+(?:is defined as|refers to|is a|is an)\\s+([^\\.\\n]{15,180})\\.");
        Matcher matcher = defPattern.matcher(text);

        while (matcher.find() && definitions.size() < 5) {
            String term = matcher.group(1).trim();
            String desc = matcher.group(2).trim();
            definitions.add(term + ": " + desc + ".");
        }
        return definitions;
    }

    private List<String> extractFormulas(String text) {
        List<String> formulas = new ArrayList<>();
        // Look for mathematical equations, Big-O, functional dependencies (X -> Y)
        Pattern formPattern = Pattern.compile("(?m)(?:(?:O\\([n1klog\\^\\s\\+\\*]+\\))|(?:[A-Z]\\s*->\\s*[A-Z])|(?:[a-zA-Z0-9_]+\\s*=\\s*[a-zA-Z0-9_\\+\\-\\*/\\^\\(\\)\\s]{3,40}))");
        Matcher matcher = formPattern.matcher(text);

        while (matcher.find() && formulas.size() < 5) {
            String f = matcher.group().trim();
            if (f.length() >= 4 && !formulas.contains(f)) {
                formulas.add(f);
            }
        }
        return formulas;
    }
}

