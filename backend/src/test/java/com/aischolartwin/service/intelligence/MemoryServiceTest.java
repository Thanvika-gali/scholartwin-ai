package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import com.aischolartwin.repository.intelligence.UserMemoryRepository;
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
class MemoryServiceTest {

    @Mock
    private UserMemoryRepository memoryRepository;

    @Mock
    private MemoryPreferenceRepository preferenceRepository;

    private MemoryService memoryService;
    private User testUser;

    @BeforeEach
    void setUp() {
        memoryService = new MemoryService(memoryRepository, preferenceRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Alice Scholar");
        testUser.setEmail("alice@test.edu");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("createMemory saves valid user memory")
    void createMemory_success() {
        MemoryDtos.CreateMemoryRequest request = new MemoryDtos.CreateMemoryRequest(
                MemoryCategory.CAREER,
                "Target Role",
                "User is targeting Principal Cloud Architect roles.",
                MemoryType.GOAL,
                MemoryProvenance.USER_EXPLICIT,
                MemorySourceType.MANUAL_ENTRY,
                "User Entry",
                1.0,
                MemoryImportance.HIGH
        );

        when(memoryRepository.save(any(UserMemory.class))).thenAnswer(inv -> {
            UserMemory m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        MemoryDtos.MemoryResponse res = memoryService.createMemory(testUser, request);

        assertNotNull(res);
        assertEquals(10L, res.id());
        assertEquals(MemoryCategory.CAREER, res.category());
        assertEquals("Target Role", res.summary());
        verify(memoryRepository).save(any(UserMemory.class));
    }

    @Test
    @DisplayName("createMemory rejects sensitive credentials and API keys")
    void createMemory_rejectsSensitiveData() {
        MemoryDtos.CreateMemoryRequest request = new MemoryDtos.CreateMemoryRequest(
                MemoryCategory.INTERACTION,
                "Leaked Credential",
                "User password is SuperSecretPassword123!",
                MemoryType.FACT,
                MemoryProvenance.USER_EXPLICIT,
                MemorySourceType.MANUAL_ENTRY,
                "User Entry",
                1.0,
                MemoryImportance.CRITICAL
        );

        assertThrows(InvalidPayloadException.class, () -> memoryService.createMemory(testUser, request));
        verify(memoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAccessibleMemoriesForUser filters out disabled categories")
    void getAccessibleMemories_filtersDisabledCategories() {
        MemoryPreference pref = new MemoryPreference(testUser);
        pref.setCareerMemoryEnabled(false); // Career disabled
        when(preferenceRepository.findByUser(testUser)).thenReturn(Optional.of(pref));

        UserMemory careerMem = new UserMemory(testUser, MemoryCategory.CAREER, "Career Mem", "Content",
                MemoryType.GOAL, MemoryProvenance.USER_EXPLICIT, MemorySourceType.MANUAL_ENTRY, "Ref", 1.0, MemoryImportance.HIGH);
        careerMem.setId(1L);

        UserMemory learningMem = new UserMemory(testUser, MemoryCategory.LEARNING, "Learning Mem", "Content",
                MemoryType.FACT, MemoryProvenance.USER_EXPLICIT, MemorySourceType.MANUAL_ENTRY, "Ref", 1.0, MemoryImportance.HIGH);
        learningMem.setId(2L);

        when(memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(testUser, MemoryStatus.ACTIVE))
                .thenReturn(List.of(careerMem, learningMem));

        List<UserMemory> accessible = memoryService.getAccessibleMemoriesForUser(testUser, null);

        assertEquals(1, accessible.size());
        assertEquals(MemoryCategory.LEARNING, accessible.get(0).getCategory());
        assertNotNull(accessible.get(0).getLastAccessedAt());
    }

    @Test
    @DisplayName("deleteMemory permanently deletes entity")
    void deleteMemory_success() {
        UserMemory mem = new UserMemory(testUser, MemoryCategory.TECHNICAL, "Skill", "Content",
                MemoryType.FACT, MemoryProvenance.USER_EXPLICIT, MemorySourceType.MANUAL_ENTRY, "Ref", 1.0, MemoryImportance.MEDIUM);
        mem.setId(42L);

        when(memoryRepository.findByIdAndUser(42L, testUser)).thenReturn(Optional.of(mem));

        memoryService.deleteMemory(testUser, 42L);

        verify(memoryRepository).delete(mem);
    }
}
