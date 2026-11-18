package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AssessmentDtos {

    public record GenerateAssessmentRequest(
            String topic,
            AssessmentDifficulty difficulty,
            AssessmentType assessmentType,
            Integer questionCount
    ) {}

    public record AssessmentQuestionDeliveryDto(
            Long id,
            String questionText,
            QuestionType questionType,
            List<String> options,
            String subtopic,
            Integer questionOrder,
            Integer points
    ) {}

    public record AssessmentDeliveryResponse(
            Long id,
            String title,
            String topic,
            String description,
            AssessmentType assessmentType,
            AssessmentDifficulty difficulty,
            Integer passingScorePercentage,
            Integer timeLimitMinutes,
            String source,
            List<AssessmentQuestionDeliveryDto> questions
    ) {}

    public record SubmitAssessmentRequest(
            Map<Long, String> userAnswers,
            Integer timeSpentSeconds
    ) {}

    public record QuestionEvaluationDto(
            Long questionId,
            String questionText,
            String submittedAnswer,
            String correctAnswer,
            boolean isCorrect,
            Integer pointsEarned,
            Integer totalPoints,
            String explanation,
            String subtopic
    ) {}

    public record AssessmentResultResponse(
            Long id,
            Long attemptId,
            String title,
            String topic,
            Integer scorePercentage,
            Integer totalPoints,
            Integer earnedPoints,
            Boolean passed,
            Integer timeSpentSeconds,
            List<QuestionEvaluationDto> questionResults,
            Map<String, Integer> topicBreakdown,
            List<String> weakAreas,
            String recommendedNextSteps,
            Instant completedAt
    ) {}

    public record AssessmentSummaryDto(
            Long id,
            String title,
            String topic,
            AssessmentType assessmentType,
            AssessmentDifficulty difficulty,
            Integer questionCount,
            Integer passingScorePercentage,
            Integer timeLimitMinutes,
            String source,
            Instant createdAt
    ) {
        public static AssessmentSummaryDto fromEntity(LearningAssessment assessment) {
            return new AssessmentSummaryDto(
                    assessment.getId(),
                    assessment.getTitle(),
                    assessment.getTopic(),
                    assessment.getAssessmentType(),
                    assessment.getDifficulty(),
                    assessment.getQuestions() != null ? assessment.getQuestions().size() : 0,
                    assessment.getPassingScorePercentage(),
                    assessment.getTimeLimitMinutes(),
                    assessment.getSource(),
                    assessment.getCreatedAt()
            );
        }
    }

    public record AssessmentAttemptSummaryDto(
            Long id,
            Long assessmentId,
            String assessmentTitle,
            String topic,
            Integer scorePercentage,
            Integer totalPoints,
            Integer earnedPoints,
            Boolean passed,
            Integer timeSpentSeconds,
            Instant completedAt
    ) {
        public static AssessmentAttemptSummaryDto fromEntity(AssessmentAttempt attempt) {
            return new AssessmentAttemptSummaryDto(
                    attempt.getId(),
                    attempt.getAssessment() != null ? attempt.getAssessment().getId() : null,
                    attempt.getAssessment() != null ? attempt.getAssessment().getTitle() : "Assessment",
                    attempt.getAssessment() != null ? attempt.getAssessment().getTopic() : "",
                    attempt.getScorePercentage(),
                    attempt.getTotalPoints(),
                    attempt.getEarnedPoints(),
                    attempt.getPassed(),
                    attempt.getTimeSpentSeconds(),
                    attempt.getCompletedAt()
            );
        }
    }
}
