package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.LearningProfileDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningPace;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LearningProfileService {

    private final LearningProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public LearningProfileService(LearningProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LearningProfile getOrCreateProfileEntity(User user) {
        return profileRepository.findByUser(user).orElseGet(() -> {
            LearningProfile newProfile = new LearningProfile(user);
            newProfile.setTargetRole("Software Development Engineer");
            newProfile.setLearningPace(LearningPace.MODERATE);
            newProfile.setDailyStudyMinutes(60);
            newProfile.setPreferredStudyTime("MORNING");
            newProfile.setActiveTopics(toJson(List.of("Data Structures & Algorithms", "Java & Spring Boot")));
            newProfile.setWeakAreas(toJson(List.of("Dynamic Programming", "System Design")));
            newProfile.setLearningInterests(toJson(List.of("Backend Development", "Cloud Architecture", "Distributed Systems")));
            newProfile.setCurrentSkillsJson(toJson(List.of("Java", "OOP", "MySQL", "Git")));
            newProfile.setRevisionIntervals("1,3,7,14,30");
            return profileRepository.save(newProfile);
        });
    }

    @Transactional
    public LearningProfileDto.Response getProfile(User user) {
        LearningProfile profile = getOrCreateProfileEntity(user);
        return toDtoResponse(profile);
    }

    @Transactional
    public LearningProfileDto.Response updateProfile(User user, LearningProfileDto.Request request) {
        LearningProfile profile = getOrCreateProfileEntity(user);

        if (request.targetRole() != null && !request.targetRole().isBlank()) {
            profile.setTargetRole(request.targetRole().trim());
        }
        if (request.learningPace() != null) {
            profile.setLearningPace(request.learningPace());
        }
        if (request.dailyStudyMinutes() != null && request.dailyStudyMinutes() > 0) {
            profile.setDailyStudyMinutes(request.dailyStudyMinutes());
        }
        if (request.preferredStudyTime() != null && !request.preferredStudyTime().isBlank()) {
            profile.setPreferredStudyTime(request.preferredStudyTime());
        }
        if (request.targetCompletionDate() != null) {
            profile.setTargetCompletionDate(request.targetCompletionDate());
        }
        if (request.learningGoals() != null) {
            profile.setLearningGoals(request.learningGoals());
        }
        if (request.activeTopics() != null) {
            profile.setActiveTopics(toJson(request.activeTopics()));
        }
        if (request.weakAreas() != null) {
            profile.setWeakAreas(toJson(request.weakAreas()));
        }
        if (request.learningInterests() != null) {
            profile.setLearningInterests(toJson(request.learningInterests()));
        }
        if (request.currentSkills() != null) {
            profile.setCurrentSkillsJson(toJson(request.currentSkills()));
        }
        if (request.revisionIntervals() != null && !request.revisionIntervals().isBlank()) {
            profile.setRevisionIntervals(request.revisionIntervals().trim());
        }

        LearningProfile saved = profileRepository.save(profile);
        return toDtoResponse(saved);
    }

    public LearningProfileDto.Response toDtoResponse(LearningProfile profile) {
        List<String> activeTopics = fromJsonList(profile.getActiveTopics());
        List<String> weakAreas = fromJsonList(profile.getWeakAreas());
        List<String> interests = fromJsonList(profile.getLearningInterests());
        List<String> skills = fromJsonList(profile.getCurrentSkillsJson());

        return LearningProfileDto.Response.fromEntity(profile, activeTopics, weakAreas, interests, skills);
    }

    private String toJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
