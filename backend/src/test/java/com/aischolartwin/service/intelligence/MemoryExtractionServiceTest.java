package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.MemoryCategory;
import com.aischolartwin.entity.intelligence.MemoryPreference;
import com.aischolartwin.entity.intelligence.MemoryStatus;
import com.aischolartwin.entity.intelligence.UserMemory;
import com.aischolartwin.entity.learning.LearningProfile;
import com.aischolartwin.repository.*;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import com.aischolartwin.repository.intelligence.UserMemoryRepository;
import com.aischolartwin.repository.learning.LearningProfileRepository;
import com.aischolartwin.repository.learning.LearningRoadmapRepository;
import com.aischolartwin.repository.learning.LearningTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryExtractionServiceTest {

    @Mock
    private UserMemoryRepository memoryRepository;

    @Mock
    private MemoryPreferenceRepository preferenceRepository;

    @Mock
    private LearningProfileRepository profileRepository;

    @Mock
    private LearningRoadmapRepository roadmapRepository;

    @Mock
    private LearningTaskRepository taskRepository;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private KnowledgeItemRepository knowledgeItemRepository;

    @Mock
    private DSAProblemRepository dsaProblemRepository;

    private MemoryExtractionService extractionService;
    private User testUser;

    @BeforeEach
    void setUp() {
        extractionService = new MemoryExtractionService(
                memoryRepository, preferenceRepository, profileRepository,
                roadmapRepository, taskRepository, careerSkillRepository,
                knowledgeItemRepository, dsaProblemRepository
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("extractMemoriesFromActivity extracts target role and weak concepts from profile")
    void extractMemories_success() {
        MemoryPreference pref = new MemoryPreference(testUser);
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(pref));

        LearningProfile profile = new LearningProfile(testUser);
        profile.setTargetRole("Distributed Systems Engineer");
        profile.setWeakAreas("Raft Consensus, Vector Clocks");
        profile.setLearningGoals("Master Distributed Consensus Algorithms");
        when(profileRepository.findByUser(testUser)).thenReturn(Optional.of(profile));

        when(memoryRepository.existsByUserAndContentIgnoreCaseAndStatusNot(eq(testUser), anyString(), eq(MemoryStatus.DELETED)))
                .thenReturn(false);

        when(memoryRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        MemoryDtos.MemoryExtractionResult result = extractionService.extractMemoriesFromActivity(testUser);

        assertNotNull(result);
        assertTrue(result.extractedCount() >= 2);
        verify(memoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("consolidateMemories removes duplicate active memories")
    void consolidateMemories_prunesDuplicates() {
        UserMemory m1 = new UserMemory(testUser, MemoryCategory.CAREER, "Role", "Target role: SDE.", null, null, null, null, 1.0, null);
        UserMemory m2 = new UserMemory(testUser, MemoryCategory.CAREER, "Role duplicate", "Target role: SDE.", null, null, null, null, 1.0, null);

        when(memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(testUser, MemoryStatus.ACTIVE))
                .thenReturn(List.of(m1, m2));

        MemoryDtos.MemoryExtractionResult result = extractionService.consolidateMemories(testUser);

        assertNotNull(result);
        assertEquals(1, result.consolidatedCount());
        verify(memoryRepository).deleteAll(anyList());
    }
}
