package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.AssessmentDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.learning.AssessmentAttemptRepository;
import com.aischolartwin.repository.learning.AssessmentQuestionRepository;
import com.aischolartwin.repository.learning.LearningAssessmentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class LearningAssessmentService {

    private final LearningAssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    public LearningAssessmentService(
            LearningAssessmentRepository assessmentRepository,
            AssessmentQuestionRepository questionRepository,
            AssessmentAttemptRepository attemptRepository,
            ObjectMapper objectMapper
    ) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AssessmentDtos.AssessmentSummaryDto> getAssessments(User user) {
        List<LearningAssessment> assessments = assessmentRepository.findByUserOrderByCreatedAtDesc(user);
        return assessments.stream().map(AssessmentDtos.AssessmentSummaryDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentDtos.AssessmentAttemptSummaryDto> getAttemptHistory(User user) {
        List<AssessmentAttempt> attempts = attemptRepository.findByUserOrderByCompletedAtDesc(user);
        return attempts.stream().map(AssessmentDtos.AssessmentAttemptSummaryDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AssessmentDtos.AssessmentDeliveryResponse getAssessmentForTestTaking(User user, Long assessmentId) {
        LearningAssessment assessment = assessmentRepository.findDetailedByIdAndUser(assessmentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));

        List<AssessmentDtos.AssessmentQuestionDeliveryDto> questionDelivery = new ArrayList<>();
        if (assessment.getQuestions() != null) {
            for (AssessmentQuestion q : assessment.getQuestions()) {
                List<String> options = parseJsonList(q.getOptionsJson());
                // Notice: correctAnswer and explanation are omitted for security
                questionDelivery.add(new AssessmentDtos.AssessmentQuestionDeliveryDto(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionType(),
                        options,
                        q.getSubtopic(),
                        q.getQuestionOrder(),
                        q.getPoints()
                ));
            }
        }

        return new AssessmentDtos.AssessmentDeliveryResponse(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getTopic(),
                assessment.getDescription(),
                assessment.getAssessmentType(),
                assessment.getDifficulty(),
                assessment.getPassingScorePercentage(),
                assessment.getTimeLimitMinutes(),
                assessment.getSource(),
                questionDelivery
        );
    }

    @Transactional
    public AssessmentDtos.AssessmentSummaryDto createOrGenerateAssessment(User user, AssessmentDtos.GenerateAssessmentRequest request) {
        String topic = request.topic() != null && !request.topic().isBlank() ? request.topic().trim() : "Data Structures & Algorithms";
        AssessmentDifficulty diff = request.difficulty() != null ? request.difficulty() : AssessmentDifficulty.INTERMEDIATE;
        AssessmentType type = request.assessmentType() != null ? request.assessmentType() : AssessmentType.TOPIC_QUIZ;
        int qCount = request.questionCount() != null && request.questionCount() > 0 ? Math.min(request.questionCount(), 15) : 5;

        LearningAssessment assessment = new LearningAssessment(
                user,
                topic + " Mastery Assessment (" + diff.name() + ")",
                topic,
                "Diagnostic assessment covering core concepts, edge cases, and practical problems in " + topic + ".",
                type,
                diff,
                70,
                Math.max(10, qCount * 3),
                "CURATED"
        );

        LearningAssessment savedAssessment = assessmentRepository.save(assessment);

        // Seed curated questions
        List<AssessmentQuestion> generatedQuestions = generateCuratedQuestions(savedAssessment, topic, diff, qCount);
        for (AssessmentQuestion q : generatedQuestions) {
            savedAssessment.addQuestion(q);
            questionRepository.save(q);
        }

        return AssessmentDtos.AssessmentSummaryDto.fromEntity(savedAssessment);
    }

    @Transactional
    public AssessmentDtos.AssessmentResultResponse submitAssessment(User user, Long assessmentId, AssessmentDtos.SubmitAssessmentRequest request) {
        LearningAssessment assessment = assessmentRepository.findDetailedByIdAndUser(assessmentId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + assessmentId));

        Map<Long, String> userAnswers = request.userAnswers() != null ? request.userAnswers() : Map.of();
        int timeSpent = request.timeSpentSeconds() != null ? request.timeSpentSeconds() : 0;

        int totalPoints = 0;
        int earnedPoints = 0;
        List<AssessmentDtos.QuestionEvaluationDto> questionResults = new ArrayList<>();
        Map<String, int[]> topicScores = new HashMap<>(); // subtopic -> [earned, total]

        for (AssessmentQuestion q : assessment.getQuestions()) {
            totalPoints += q.getPoints();
            String submitted = userAnswers.get(q.getId());
            boolean isCorrect = isAnswerMatching(submitted, q.getCorrectAnswer(), q.getQuestionType());

            int pointsEarned = isCorrect ? q.getPoints() : 0;
            earnedPoints += pointsEarned;

            String subtopic = q.getSubtopic() != null ? q.getSubtopic() : assessment.getTopic();
            int[] scoreArr = topicScores.computeIfAbsent(subtopic, k -> new int[2]);
            scoreArr[0] += pointsEarned;
            scoreArr[1] += q.getPoints();

            questionResults.add(new AssessmentDtos.QuestionEvaluationDto(
                    q.getId(),
                    q.getQuestionText(),
                    submitted != null ? submitted : "No answer submitted",
                    q.getCorrectAnswer(),
                    isCorrect,
                    pointsEarned,
                    q.getPoints(),
                    q.getExplanation() != null ? q.getExplanation() : "Correct answer: " + q.getCorrectAnswer(),
                    subtopic
            ));
        }

        int scorePercentage = totalPoints > 0 ? (int) Math.round(((double) earnedPoints / totalPoints) * 100) : 0;
        boolean passed = scorePercentage >= assessment.getPassingScorePercentage();

        // Calculate subtopic percentage breakdown & identify weak areas
        Map<String, Integer> topicBreakdown = new HashMap<>();
        List<String> weakAreas = new ArrayList<>();

        for (Map.Entry<String, int[]> entry : topicScores.entrySet()) {
            int subTotal = entry.getValue()[1];
            int subEarned = entry.getValue()[0];
            int subPct = subTotal > 0 ? (int) Math.round(((double) subEarned / subTotal) * 100) : 0;
            topicBreakdown.put(entry.getKey(), subPct);
            if (subPct < 65) {
                weakAreas.add(entry.getKey());
            }
        }

        // Generate tailored next steps based on score
        String nextSteps;
        if (passed) {
            nextSteps = "Excellent work! You demonstrated strong conceptual understanding of " + assessment.getTopic() +
                    ". Consider advancing to " + (assessment.getDifficulty() == AssessmentDifficulty.BEGINNER ? "INTERMEDIATE" : "ADVANCED") +
                    " assessment, or practicing related problems on DSA Tracker.";
        } else {
            nextSteps = "Identified gaps in: " + String.join(", ", weakAreas.isEmpty() ? List.of(assessment.getTopic()) : weakAreas) +
                    ". Recommended: Review Knowledge Hub notes, schedule a spaced repetition revision, and re-attempt the quiz.";
        }

        // Persist attempt
        AssessmentAttempt attempt = new AssessmentAttempt(
                user,
                assessment,
                Instant.now().minusSeconds(timeSpent),
                Instant.now(),
                scorePercentage,
                totalPoints,
                earnedPoints,
                passed,
                timeSpent,
                toJson(userAnswers),
                toJson(questionResults),
                toJson(topicBreakdown),
                toJson(weakAreas),
                nextSteps
        );
        AssessmentAttempt savedAttempt = attemptRepository.save(attempt);

        return new AssessmentDtos.AssessmentResultResponse(
                assessment.getId(),
                savedAttempt.getId(),
                assessment.getTitle(),
                assessment.getTopic(),
                scorePercentage,
                totalPoints,
                earnedPoints,
                passed,
                timeSpent,
                questionResults,
                topicBreakdown,
                weakAreas,
                nextSteps,
                savedAttempt.getCompletedAt()
        );
    }

    private boolean isAnswerMatching(String submitted, String correct, QuestionType type) {
        if (submitted == null || correct == null) return false;
        String s = submitted.trim().toLowerCase();
        String c = correct.trim().toLowerCase();
        if (type == QuestionType.TRUE_FALSE) {
            if (s.startsWith("t") && c.startsWith("t")) return true;
            if (s.startsWith("f") && c.startsWith("f")) return true;
        }
        return s.equals(c);
    }

    private List<AssessmentQuestion> generateCuratedQuestions(LearningAssessment assessment, String topic, AssessmentDifficulty diff, int count) {
        List<AssessmentQuestion> bank = getCuratedQuestionBank(assessment, topic);
        if (bank.size() <= count) return bank;
        return bank.subList(0, count);
    }

    private List<AssessmentQuestion> getCuratedQuestionBank(LearningAssessment assessment, String topic) {
        String lower = topic.toLowerCase();
        List<AssessmentQuestion> list = new ArrayList<>();

        if (lower.contains("java") || lower.contains("oop")) {
            list.add(new AssessmentQuestion(assessment,
                    "In Java, what is the default capacity of an ArrayList when it is instantiated without arguments, and how does it resize?",
                    QuestionType.MCQ,
                    toJson(List.of("Default capacity 10; grows by 50% (newCapacity = oldCapacity + oldCapacity >> 1)",
                            "Default capacity 16; doubles (newCapacity = oldCapacity * 2)",
                            "Default capacity 0; grows by 1 per element",
                            "Default capacity 32; doubles")),
                    "Default capacity 10; grows by 50% (newCapacity = oldCapacity + oldCapacity >> 1)",
                    "In JDK 8+, ArrayList starts empty and initializes to 10 elements on first add. It grows by approximately 50% via bit-shift.",
                    "Java Memory & Collections", 1, 1));

            list.add(new AssessmentQuestion(assessment,
                    "Which mechanism ensures thread safety in ConcurrentHashMap during concurrent writes?",
                    QuestionType.MCQ,
                    toJson(List.of("Lock-free CAS for node insertion and synchronized on bucket head node",
                            "A single global ReentrantLock across the whole map",
                            "ReadWriteLock on every get() call",
                            "Vector-style method-level synchronized keywords")),
                    "Lock-free CAS for node insertion and synchronized on bucket head node",
                    "JDK 8+ ConcurrentHashMap uses CAS operations for empty bucket insertions and synchronized locks solely on the head node of the specific bin.",
                    "Java Concurrency", 2, 1));

            list.add(new AssessmentQuestion(assessment,
                    "True or False: In Java, string literals with identical content are stored in the String Constant Pool to conserve heap memory.",
                    QuestionType.TRUE_FALSE,
                    toJson(List.of("True", "False")),
                    "True",
                    "String interning ensures literals with identical character sequences reuse the same object in the String Constant Pool.",
                    "Java Memory Model", 3, 1));

            list.add(new AssessmentQuestion(assessment,
                    "What is the time complexity of searching for an element in a HashMap with optimal hashCode distribution?",
                    QuestionType.MCQ,
                    toJson(List.of("O(1) average time complexity", "O(log N)", "O(N)", "O(N log N)")),
                    "O(1) average time complexity",
                    "With uniform hash distribution and minimal collisions, HashMap achieves O(1) average constant lookup time.",
                    "Hashing & Maps", 4, 1));

            list.add(new AssessmentQuestion(assessment,
                    "What happens if two unequal objects return the same hashCode() in Java?",
                    QuestionType.MCQ,
                    toJson(List.of("A collision occurs; equals() is used to differentiate them in the bucket",
                            "A compile-time error occurs",
                            "The JVM throws a ConcurrentModificationException",
                            "The previous object is overwritten")),
                    "A collision occurs; equals() is used to differentiate them in the bucket",
                    "Collisions are normal in hashing. The bucket stores multiple nodes (linked list or red-black tree) and uses equals() to find the exact key.",
                    "Java OOP Principles", 5, 1));
        } else if (lower.contains("sql") || lower.contains("database")) {
            list.add(new AssessmentQuestion(assessment,
                    "Which index structure is standard in MySQL InnoDB for primary keys and secondary indexes?",
                    QuestionType.MCQ,
                    toJson(List.of("B+ Tree", "Hash Table", "LSM Tree", "Binary Search Tree")),
                    "B+ Tree",
                    "InnoDB utilizes B+ Trees where leaf nodes contain actual row data (clustered index) or primary key pointers (secondary indexes).",
                    "Database Indexing", 1, 1));

            list.add(new AssessmentQuestion(assessment,
                    "True or False: In a composite index on (user_id, status, created_at), a query filtering ONLY on created_at can effectively utilize the index.",
                    QuestionType.TRUE_FALSE,
                    toJson(List.of("True", "False")),
                    "False",
                    "B+ Tree composite indexes require the leftmost prefix. Filtering solely on the 3rd column bypasses the index.",
                    "SQL Optimization", 2, 1));

            list.add(new AssessmentQuestion(assessment,
                    "What does the 'I' in ACID database transactions represent?",
                    QuestionType.MCQ,
                    toJson(List.of("Isolation", "Idempotency", "Integrity", "Indexing")),
                    "Isolation",
                    "ACID stands for Atomicity, Consistency, Isolation, and Durability.",
                    "Transaction Mechanics", 3, 1));
        } else {
            // General DSA questions
            list.add(new AssessmentQuestion(assessment,
                    "What is the optimal time complexity to detect a cycle in a singly linked list using Floyd's Cycle-Finding algorithm?",
                    QuestionType.MCQ,
                    toJson(List.of("O(N) time and O(1) space", "O(N log N) time and O(N) space", "O(N^2) time and O(1) space", "O(1) time and O(N) space")),
                    "O(N) time and O(1) space",
                    "Floyd's algorithm uses fast and slow pointers, traversing the list in linear time with zero extra space.",
                    "Linked Lists", 1, 1));

            list.add(new AssessmentQuestion(assessment,
                    "Which traversal of a Binary Search Tree produces elements in sorted non-decreasing order?",
                    QuestionType.MCQ,
                    toJson(List.of("In-order traversal (Left, Root, Right)", "Pre-order traversal", "Post-order traversal", "Level-order traversal")),
                    "In-order traversal (Left, Root, Right)",
                    "In-order traversal visits the left subtree, then the current node, then the right subtree, producing ascending values for any valid BST.",
                    "Trees & Binary Search", 2, 1));

            list.add(new AssessmentQuestion(assessment,
                    "True or False: Dynamic Programming is applicable when a problem exhibits both Overlapping Subproblems and Optimal Substructure.",
                    QuestionType.TRUE_FALSE,
                    toJson(List.of("True", "False")),
                    "True",
                    "Optimal substructure allows building the solution from subproblems, and overlapping subproblems makes memoization/tabulation effective.",
                    "Dynamic Programming", 3, 1));

            list.add(new AssessmentQuestion(assessment,
                    "What is the average time complexity of QuickSort with a randomized pivot?",
                    QuestionType.MCQ,
                    toJson(List.of("O(N log N)", "O(N^2)", "O(N)", "O(log N)")),
                    "O(N log N)",
                    "Randomized QuickSort splits the array evenly on average, yielding O(N log N) time complexity.",
                    "Sorting & Searching", 4, 1));

            list.add(new AssessmentQuestion(assessment,
                    "Which data structure is optimal for finding the running median of a data stream?",
                    QuestionType.MCQ,
                    toJson(List.of("Two Heaps (Max-Heap for lower half, Min-Heap for upper half)",
                            "A single balanced array",
                            "A Queue",
                            "A Stack")),
                    "Two Heaps (Max-Heap for lower half, Min-Heap for upper half)",
                    "Two heaps allow O(log N) element insertion and O(1) median retrieval by keeping the heaps balanced.",
                    "Heaps & Priority Queues", 5, 1));
        }

        return list;
    }

    private String toJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
