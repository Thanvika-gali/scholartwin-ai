package com.aischolartwin.dto.learning;

import com.aischolartwin.entity.learning.LearningPace;
import com.aischolartwin.entity.learning.LearningProfile;

import java.time.LocalDate;
import java.util.List;

public class LearningProfileDto {

    public record Request(
            String targetRole,
            LearningPace learningPace,
            Integer dailyStudyMinutes,
            String preferredStudyTime,
            LocalDate targetCompletionDate,
            String learningGoals,
            List<String> activeTopics,
            List<String> weakAreas,
            List<String> learningInterests,
            List<String> currentSkills,
            String revisionIntervals
    ) {}

    public record Response(
            Long id,
            String targetRole,
            LearningPace learningPace,
            Integer dailyStudyMinutes,
            String preferredStudyTime,
            LocalDate targetCompletionDate,
            String learningGoals,
            List<String> activeTopics,
            List<String> weakAreas,
            List<String> learningInterests,
            List<String> currentSkills,
            String revisionIntervals
    ) {
        public static Response fromEntity(LearningProfile entity, List<String> activeTopicsList, List<String> weakAreasList, List<String> interestsList, List<String> skillsList) {
            return new Response(
                    entity.getId(),
                    entity.getTargetRole(),
                    entity.getLearningPace(),
                    entity.getDailyStudyMinutes(),
                    entity.getPreferredStudyTime(),
                    entity.getTargetCompletionDate(),
                    entity.getLearningGoals(),
                    activeTopicsList,
                    weakAreasList,
                    interestsList,
                    skillsList,
                    entity.getRevisionIntervals()
            );
        }
    }
}
