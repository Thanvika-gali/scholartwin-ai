package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.AssessmentDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.*;
import com.aischolartwin.repository.learning.AssessmentAttemptRepository;
import com.aischolartwin.repository.learning.AssessmentQuestionRepository;
import com.aischolartwin.repository.learning.LearningAssessmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningAssessmentServiceTest {

    @Mock
    private LearningAssessmentRepository assessmentRepository;

    @Mock
    private AssessmentQuestionRepository questionRepository;

    @Mock
    private AssessmentAttemptRepository attemptRepository;

    private LearningAssessmentService assessmentService;
    private User testUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        assessmentService = new LearningAssessmentService(
                assessmentRepository,
                questionRepository,
                attemptRepository,
                objectMapper
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Eve Tester");
        testUser.setEmail("eve@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("getAssessmentForTestTaking strictly hides correct answers and explanations")
    void getAssessmentForTestTaking_hidesAnswersAndExplanations() {
        LearningAssessment assessment = new LearningAssessment(
                testUser,
                "Java Concurrency Diagnostic",
                "Java",
                "Assessing thread synchronization and atomic invariants",
                AssessmentType.TOPIC_QUIZ,
                AssessmentDifficulty.INTERMEDIATE,
                70,
                15,
                "SYSTEM"
        );
        assessment.setId(10L);

        AssessmentQuestion q1 = new AssessmentQuestion(
                assessment,
                "What is the memory visibility guarantee provided by the volatile keyword?",
                QuestionType.MCQ,
                "[\"Mutual exclusion\", \"Happens-before visibility\", \"Atomicity of compound actions\", \"Deadlock prevention\"]",
                "Happens-before visibility",
                "Volatile ensures that writes to a variable are immediately visible to other threads.",
                "Java Concurrency",
                1,
                1
        );
        q1.setId(101L);

        assessment.setQuestions(List.of(q1));

        when(assessmentRepository.findDetailedByIdAndUser(10L, testUser)).thenReturn(Optional.of(assessment));

        AssessmentDtos.AssessmentDeliveryResponse delivery = assessmentService.getAssessmentForTestTaking(testUser, 10L);

        assertNotNull(delivery);
        assertEquals(1, delivery.questions().size());
        AssessmentDtos.AssessmentQuestionDeliveryDto deliveredQuestion = delivery.questions().get(0);

        // Security check: Verify delivered question payload
        assertEquals(101L, deliveredQuestion.id());
        assertEquals("What is the memory visibility guarantee provided by the volatile keyword?", deliveredQuestion.questionText());
        assertEquals(4, deliveredQuestion.options().size());
        assertNotNull(deliveredQuestion.options());
    }

    @Test
    @DisplayName("submitAssessment computes score percentage and returns full explanations")
    void submitAssessment_gradesCorrectlyAndRevealsExplanations() {
        LearningAssessment assessment = new LearningAssessment(
                testUser,
                "SQL Indexing Quiz",
                "SQL",
                "Assessing B-Tree vs Hash index tradeoffs",
                AssessmentType.TOPIC_QUIZ,
                AssessmentDifficulty.BEGINNER,
                70,
                10,
                "SYSTEM"
        );
        assessment.setId(20L);

        AssessmentQuestion q1 = new AssessmentQuestion(
                assessment,
                "Which index structure is best for range queries?",
                QuestionType.MCQ,
                "[\"Hash Index\", \"B-Tree / B+Tree Index\", \"Bitmap Index\", \"Spatial Index\"]",
                "B-Tree / B+Tree Index",
                "B-Trees maintain ordered key pages allowing efficient range scans.",
                "SQL Indexing",
                1,
                1
        );
        q1.setId(201L);

        AssessmentQuestion q2 = new AssessmentQuestion(
                assessment,
                "Primary keys are automatically indexed in relational databases.",
                QuestionType.TRUE_FALSE,
                "[\"True\", \"False\"]",
                "True",
                "DBMS engines automatically create unique clustered/secondary indexes on primary keys.",
                "SQL Indexing",
                2,
                1
        );
        q2.setId(202L);

        assessment.setQuestions(List.of(q1, q2));

        when(assessmentRepository.findDetailedByIdAndUser(20L, testUser)).thenReturn(Optional.of(assessment));
        when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
            AssessmentAttempt a = inv.getArgument(0);
            a.setId(999L);
            return a;
        });

        // Student answers: Q1 correct, Q2 wrong
        Map<Long, String> answers = Map.of(
                201L, "B-Tree / B+Tree Index",
                202L, "False"
        );

        AssessmentDtos.SubmitAssessmentRequest submitRequest = new AssessmentDtos.SubmitAssessmentRequest(answers, 300);

        AssessmentDtos.AssessmentResultResponse result = assessmentService.submitAssessment(testUser, 20L, submitRequest);

        assertNotNull(result);
        assertEquals(1, result.earnedPoints());
        assertEquals(2, result.totalPoints());
        assertEquals(50, result.scorePercentage());
        assertFalse(result.passed(), "50% should be below 70% passing threshold");

        // Verify that in the submitted results, explanations ARE included
        assertEquals(2, result.questionResults().size());
        assertTrue(result.questionResults().get(0).isCorrect());
        assertFalse(result.questionResults().get(1).isCorrect());
        assertEquals("B-Tree / B+Tree Index", result.questionResults().get(0).correctAnswer());
        assertEquals("True", result.questionResults().get(1).correctAnswer());
        assertNotNull(result.questionResults().get(0).explanation());
    }
}
