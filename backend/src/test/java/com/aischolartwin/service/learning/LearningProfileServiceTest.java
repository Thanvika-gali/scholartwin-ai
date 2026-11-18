package com.aischolartwin.service.learning;

import com.aischolartwin.dto.learning.LearningProfileDto;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningPace;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningProfileServiceTest {

    @Mock
    private LearningProfileRepository profileRepository;

    private LearningProfileService profileService;
    private User testUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        profileService = new LearningProfileService(profileRepository, objectMapper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Alice Engineer");
        testUser.setEmail("alice@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("getProfile creates and returns default profile if none exists")
    void getProfile_createsDefaultIfMissing() {
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(profileRepository.save(any(LearningProfile.class))).thenAnswer(invocation -> {
            LearningProfile p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        LearningProfileDto.Response response = profileService.getProfile(testUser);

        assertNotNull(response);
        assertEquals("Software Development Engineer", response.targetRole());
        assertEquals(LearningPace.MODERATE, response.learningPace());
        assertEquals(60, response.dailyStudyMinutes());
        assertTrue(response.activeTopics().contains("Data Structures & Algorithms"));
        verify(profileRepository).save(any(LearningProfile.class));
    }

    @Test
    @DisplayName("updateProfile correctly persists updated pace, role, and skills")
    void updateProfile_updatesFieldsCorrectly() {
        LearningProfile existing = new LearningProfile(testUser);
        existing.setId(5L);
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(LearningProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearningProfileDto.Request request = new LearningProfileDto.Request(
                "Full Stack Engineer",
                LearningPace.INTENSIVE,
                120,
                "EVENING",
                LocalDate.now().plusMonths(6),
                "Master React and Microservices",
                List.of("React", "Spring Boot", "Kafka"),
                List.of("CSS Grid", "Kubernetes"),
                List.of("Web Architecture"),
                List.of("TypeScript", "Java", "Docker"),
                "1,2,5,10,20"
        );

        LearningProfileDto.Response response = profileService.updateProfile(testUser, request);

        assertNotNull(response);
        assertEquals("Full Stack Engineer", response.targetRole());
        assertEquals(LearningPace.INTENSIVE, response.learningPace());
        assertEquals(120, response.dailyStudyMinutes());
        assertEquals("EVENING", response.preferredStudyTime());
        assertTrue(response.activeTopics().contains("React"));
        assertTrue(response.weakAreas().contains("Kubernetes"));
        assertEquals("1,2,5,10,20", response.revisionIntervals());
    }
}
