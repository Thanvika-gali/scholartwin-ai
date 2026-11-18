package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.MemoryDtos;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import com.aischolartwin.repository.intelligence.UserMemoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MemoryService {

    private static final Logger log = LoggerFactory.getLogger(MemoryService.class);

    private final UserMemoryRepository memoryRepository;
    private final MemoryPreferenceRepository preferenceRepository;

    // Layered multi-pattern secret & credential detection
    private static final Pattern SENSITIVE_DATA_PATTERN = Pattern.compile(
            "(?i)(password\\s*[:=]\\s*\\S+|secret[_-]?key\\s*[:=]\\s*\\S+|api[_-]?key\\s*[:=]\\s*[a-zA-Z0-9_-]{12,}|" +
            "bearer\\s+[a-zA-Z0-9_.-]{20,}|eyJh[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}\\.[a-zA-Z0-9_-]{10,}|" +
            "AIza[0-9A-Za-z-_]{30,40}|AKIA[0-9A-Z]{16}|-----BEGIN [A-Z\\s]*PRIVATE KEY-----|(jdbc:mysql|mongodb|postgres|redis):\\/\\/[^\\s]+|" +
            "password|secret[_-]?key|private[_-]?key)"
    );

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore\\s+(all\\s+)?(previous|prior)\\s+instructions|system\\s+prompt|disregard\\s+rules|dan\\s+mode|jailbreak)",
            Pattern.CASE_INSENSITIVE
    );

    public MemoryService(UserMemoryRepository memoryRepository,
                         MemoryPreferenceRepository preferenceRepository) {
        this.memoryRepository = memoryRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional(readOnly = true)
    public List<MemoryDtos.MemoryResponse> getMemories(User user, MemoryCategory category, MemoryStatus status, Boolean pinnedOnly) {
        MemoryStatus targetStatus = status != null ? status : MemoryStatus.ACTIVE;
        List<UserMemory> memories;

        if (Boolean.TRUE.equals(pinnedOnly)) {
            memories = memoryRepository.findByUserAndIsPinnedTrueAndStatusOrderByUpdatedAtDesc(user, targetStatus);
        } else if (category != null) {
            memories = memoryRepository.findByUserAndCategoryAndStatusOrderByIsPinnedDescUpdatedAtDesc(user, category, targetStatus);
        } else {
            memories = memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(user, targetStatus);
        }

        return memories.stream().map(MemoryDtos.MemoryResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public MemoryDtos.MemoryResponse getMemory(User user, Long id) {
        UserMemory memory = memoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with ID: " + id));
        return MemoryDtos.MemoryResponse.fromEntity(memory);
    }

    @Transactional
    public MemoryDtos.MemoryResponse createMemory(User user, MemoryDtos.CreateMemoryRequest request) {
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidPayloadException("Memory content cannot be blank.");
        }
        validateNoSensitiveData(request.content());

        String summary = request.summary();
        if (summary == null || summary.isBlank()) {
            summary = generateSummary(request.content());
        }

        UserMemory memory = new UserMemory(
                user,
                request.category(),
                summary.trim(),
                request.content().trim(),
                request.memoryType() != null ? request.memoryType() : MemoryType.FACT,
                request.provenance() != null ? request.provenance() : MemoryProvenance.USER_EXPLICIT,
                request.sourceType() != null ? request.sourceType() : MemorySourceType.MANUAL_ENTRY,
                request.sourceReference() != null ? request.sourceReference().trim() : "User Added",
                request.confidence() != null ? request.confidence() : 1.0,
                request.importance() != null ? request.importance() : MemoryImportance.MEDIUM
        );

        UserMemory saved = memoryRepository.save(memory);
        return MemoryDtos.MemoryResponse.fromEntity(saved);
    }

    @Transactional
    public MemoryDtos.MemoryResponse updateMemory(User user, Long id, MemoryDtos.UpdateMemoryRequest request) {
        UserMemory memory = memoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with ID: " + id));

        if (request.content() != null && !request.content().isBlank()) {
            validateNoSensitiveData(request.content());
            memory.setContent(request.content().trim());
        }
        if (request.summary() != null && !request.summary().isBlank()) {
            memory.setSummary(request.summary().trim());
        }
        if (request.category() != null) {
            memory.setCategory(request.category());
        }
        if (request.memoryType() != null) {
            memory.setMemoryType(request.memoryType());
        }
        if (request.importance() != null) {
            memory.setImportance(request.importance());
        }
        if (request.status() != null) {
            memory.setStatus(request.status());
        }
        if (request.isPinned() != null) {
            memory.setIsPinned(request.isPinned());
        }

        UserMemory saved = memoryRepository.save(memory);
        return MemoryDtos.MemoryResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteMemory(User user, Long id) {
        UserMemory memory = memoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with ID: " + id));
        // Hard deletion so it is immediately removed from database and excluded from AI retrieval
        memoryRepository.delete(memory);
        log.info("Permanently deleted memory ID: {} for user: {}", id, user.getEmail());
    }

    @Transactional
    public MemoryDtos.MemoryResponse archiveMemory(User user, Long id) {
        UserMemory memory = memoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with ID: " + id));

        if (memory.getStatus() == MemoryStatus.ACTIVE) {
            memory.setStatus(MemoryStatus.ARCHIVED);
        } else {
            memory.setStatus(MemoryStatus.ACTIVE);
        }

        UserMemory saved = memoryRepository.save(memory);
        return MemoryDtos.MemoryResponse.fromEntity(saved);
    }

    @Transactional
    public MemoryDtos.MemoryResponse togglePin(User user, Long id) {
        UserMemory memory = memoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found with ID: " + id));

        memory.setIsPinned(!Boolean.TRUE.equals(memory.getIsPinned()));
        UserMemory saved = memoryRepository.save(memory);
        return MemoryDtos.MemoryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MemoryDtos.MemoryResponse> searchMemories(User user, String query) {
        if (query == null || query.isBlank()) {
            return getMemories(user, null, MemoryStatus.ACTIVE, false);
        }
        return memoryRepository.searchMemories(user, MemoryStatus.ACTIVE, query.trim())
                .stream()
                .map(MemoryDtos.MemoryResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void bulkDelete(User user, MemoryDtos.BulkDeleteRequest request) {
        if (Boolean.TRUE.equals(request.clearAll())) {
            memoryRepository.deleteByUser(user);
            log.info("Cleared all memories for user: {}", user.getEmail());
            return;
        }

        if (request.memoryIds() != null && !request.memoryIds().isEmpty()) {
            for (Long id : request.memoryIds()) {
                memoryRepository.findByIdAndUser(id, user).ifPresent(memoryRepository::delete);
            }
            log.info("Bulk deleted {} memories for user: {}", request.memoryIds().size(), user.getEmail());
        }
    }

    @Transactional
    public List<UserMemory> getAccessibleMemoriesForUser(User user, List<MemoryCategory> requestedCategories) {
        MemoryPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> preferenceRepository.save(new MemoryPreference(user)));

        if (!Boolean.TRUE.equals(pref.getMemoryEnabled())) {
            return List.of();
        }

        List<UserMemory> activeMemories = memoryRepository.findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(user, MemoryStatus.ACTIVE);
        List<UserMemory> accessible = new ArrayList<>();
        Instant now = Instant.now();

        for (UserMemory m : activeMemories) {
            if (!pref.isCategoryEnabled(m.getCategory())) {
                continue;
            }
            if (requestedCategories != null && !requestedCategories.isEmpty() && !requestedCategories.contains(m.getCategory())) {
                continue;
            }
            if (m.getContent() != null && INJECTION_PATTERN.matcher(m.getContent()).find()) {
                m.setContent(INJECTION_PATTERN.matcher(m.getContent()).replaceAll("[FILTERED_INJECTION]"));
            }
            m.setLastAccessedAt(now);
            accessible.add(m);
        }

        if (!accessible.isEmpty()) {
            memoryRepository.saveAll(accessible);
        }
        return accessible;
    }

    @Transactional(readOnly = true)
    public MemoryDtos.MemoryStatsResponse getMemoryStats(User user) {
        List<UserMemory> all = memoryRepository.findByUser(user);
        long total = all.size();
        long active = all.stream().filter(m -> m.getStatus() == MemoryStatus.ACTIVE).count();
        long archived = all.stream().filter(m -> m.getStatus() == MemoryStatus.ARCHIVED).count();
        long pinned = all.stream().filter(m -> Boolean.TRUE.equals(m.getIsPinned()) && m.getStatus() == MemoryStatus.ACTIVE).count();
        return new MemoryDtos.MemoryStatsResponse(total, active, archived, pinned);
    }

    private void validateNoSensitiveData(String text) {
        if (text == null) return;
        if (SENSITIVE_DATA_PATTERN.matcher(text).find()) {
            throw new InvalidPayloadException("Security Warning: Memory cannot contain passwords, private API keys, or security tokens.");
        }
    }

    public static String redactSensitiveData(String text) {
        if (text == null) return null;
        return SENSITIVE_DATA_PATTERN.matcher(text).replaceAll("[REDACTED_SECRET]");
    }

    public static String sanitizeMemoryContent(String text) {
        if (text == null) return null;
        return INJECTION_PATTERN.matcher(text).replaceAll("[FILTERED_INJECTION]");
    }

    private String generateSummary(String content) {
        if (content.length() <= 60) {
            return content;
        }
        int spaceIdx = content.indexOf(' ', 50);
        if (spaceIdx > 0 && spaceIdx <= 70) {
            return content.substring(0, spaceIdx) + "...";
        }
        return content.substring(0, 60) + "...";
    }
}
