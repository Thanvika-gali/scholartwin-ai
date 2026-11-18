package com.aischolartwin.dto.learning;

import java.util.List;

public class AiLearningAssistantDtos {

    public enum QueryContextType {
        CONCEPT_EXPLANATION,
        EXAMPLE_GENERATION,
        PRACTICE_QUESTIONS,
        REVISION_ADVICE,
        ASSESSMENT_REVIEW,
        NEXT_TOPIC_RECOMMENDATION,
        GENERAL_QUERY
    }

    public record Request(
            String query,
            String topic,
            QueryContextType contextType,
            Long relatedRoadmapId,
            Long relatedTaskId,
            Long relatedAssessmentAttemptId
    ) {}

    public record Response(
            String responseText,
            String topic,
            String suggestedNextTopic,
            List<String> keyTakeaways,
            List<String> practicalExercises,
            String sourceGroundingMode, // "PERSONAL_KNOWLEDGE_GROUNDED", "RULE_BASED_ASSISTANT", "GEMINI_AI"
            String limitationsNote
    ) {}
}
