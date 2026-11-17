package com.aischolartwin.service;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.UserNotFoundException;
import com.aischolartwin.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Service managing user profile queries, settings center, data export, and account lifecycle.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileStorageService storageService;
    private final DSAProblemRepository dsaProblemRepository;
    private final DSAAttemptRepository dsaAttemptRepository;
    private final DSARevisionRepository dsaRevisionRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository;
    private final CareerActionRepository careerActionRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ObjectMapper objectMapper,
                       KnowledgeItemRepository knowledgeItemRepository,
                       DocumentRepository documentRepository,
                       DocumentChunkRepository chunkRepository,
                       FileStorageService storageService,
                       DSAProblemRepository dsaProblemRepository,
                       DSAAttemptRepository dsaAttemptRepository,
                       DSARevisionRepository dsaRevisionRepository,
                       ResumeRepository resumeRepository,
                       ResumeAnalysisRepository resumeAnalysisRepository,
                       CareerSkillRepository careerSkillRepository,
                       JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository,
                       CareerActionRepository careerActionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.dsaProblemRepository = dsaProblemRepository;
        this.dsaAttemptRepository = dsaAttemptRepository;
        this.dsaRevisionRepository = dsaRevisionRepository;
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.jobDescriptionAnalysisRepository = jobDescriptionAnalysisRepository;
        this.careerActionRepository = careerActionRepository;
    }

    public UserService(UserRepository userRepository) {
        this(userRepository, null, new ObjectMapper(), null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Retrieves the profile of the user identified by email.
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = getByEmail(email);
        return UserResponse.fromEntity(user);
    }

    /**
     * Retrieves the User entity by email.
     */
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Updates legacy fullName profile request.
     */
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Updates comprehensive profile information.
     */
    @Transactional
    public UserResponse updateFullProfile(String email, UserProfileUpdateRequest request) {
        User user = getByEmail(email);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        user.setInstitution(request.institution() != null ? request.institution().trim() : null);
        user.setDegree(request.degree() != null ? request.degree().trim() : null);
        user.setGraduationYear(request.graduationYear());
        user.setAcademicYear(request.academicYear() != null ? request.academicYear().trim() : null);
        user.setCurrentSemester(request.currentSemester());
        user.setBio(request.bio() != null ? request.bio().trim() : null);
        user.setAvatarUrl(request.avatarUrl() != null ? request.avatarUrl().trim() : null);
        user.setGithubUrl(request.githubUrl() != null ? request.githubUrl().trim() : null);
        user.setLinkedinUrl(request.linkedinUrl() != null ? request.linkedinUrl().trim() : null);
        user.setPortfolioUrl(request.portfolioUrl() != null ? request.portfolioUrl().trim() : null);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Updates Academic Preferences.
     */
    @Transactional
    public UserResponse updateAcademicPreferences(String email, AcademicPreferencesDto dto) {
        User user = getByEmail(email);
        try {
            user.setAcademicPreferencesJson(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new InvalidPayloadException("Invalid academic preferences format.");
        }
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Updates AI Preferences.
     */
    @Transactional
    public UserResponse updateAiPreferences(String email, AiPreferencesDto dto) {
        User user = getByEmail(email);
        try {
            user.setAiPreferencesJson(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new InvalidPayloadException("Invalid AI preferences format.");
        }
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Updates Notification Preferences.
     */
    @Transactional
    public UserResponse updateNotificationPreferences(String email, NotificationPreferencesDto dto) {
        User user = getByEmail(email);
        try {
            user.setNotificationPreferencesJson(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new InvalidPayloadException("Invalid notification preferences format.");
        }
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Updates UI theme preference.
     */
    @Transactional
    public UserResponse updateThemePreference(String email, String theme) {
        User user = getByEmail(email);
        String cleanTheme = theme != null ? theme.trim().toUpperCase() : "SYSTEM";
        if (!Set.of("DARK", "LIGHT", "SYSTEM").contains(cleanTheme)) {
            cleanTheme = "SYSTEM";
        }
        user.setThemePreference(cleanTheme);
        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Changes user password verifying the current password first.
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getByEmail(email);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPayloadException("Current password does not match.");
        }

        if (request.newPassword() == null || request.newPassword().length() < 8) {
            throw new InvalidPayloadException("New password must be at least 8 characters long.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user {}", user.getEmail());
    }

    /**
     * Aggregates and exports all user data across all modules in JSON.
     */
    @Transactional(readOnly = true)
    public DataExportResponse exportUserData(String email) {
        User user = getByEmail(email);

        List<KnowledgeItemResponse> knowledgeItems = knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream().map(KnowledgeItemResponse::fromEntity).toList();

        List<DocumentResponse> documents = documentRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream().map(DocumentResponse::fromEntity).toList();

        List<DSAProblemResponse> dsaProblems = dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream().map(DSAProblemResponse::fromEntity).toList();

        List<CareerSkillResponse> careerSkills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user)
                .stream().map(CareerSkillResponse::fromEntity).toList();

        List<JobDescriptionAnalysisResponse> jobAnalyses = jobDescriptionAnalysisRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(this::toJobAnalysisResponse).toList();

        List<CareerActionResponse> careerActions = careerActionRepository.findByUserOrderBySuggestedDeadlineAscCreatedAtDesc(user)
                .stream().map(CareerActionResponse::fromEntity).toList();

        long resumesCount = resumeRepository.countByUser(user);
        long dsaAttemptsCount = dsaAttemptRepository.countByUser(user);

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("knowledgeItems", (long) knowledgeItems.size());
        counts.put("documents", (long) documents.size());
        counts.put("dsaProblems", (long) dsaProblems.size());
        counts.put("dsaAttempts", dsaAttemptsCount);
        counts.put("resumes", resumesCount);
        counts.put("careerSkills", (long) careerSkills.size());
        counts.put("jobAnalyses", (long) jobAnalyses.size());
        counts.put("careerActions", (long) careerActions.size());

        return new DataExportResponse(
                "AI ScholarTwin",
                "1.0.0",
                Instant.now(),
                UserResponse.fromEntity(user),
                counts,
                knowledgeItems,
                documents,
                dsaProblems,
                careerSkills,
                jobAnalyses,
                careerActions
        );
    }

    /**
     * Deletes user account and cascades deletion across all tables and physical disk files.
     */
    @Transactional
    public void deleteAccount(String email) {
        User user = getByEmail(email);
        log.warn("Initiating account deletion for user: {}", email);

        // 1. Delete Documents & Disk Files
        List<Document> documents = documentRepository.findByUserOrderByUpdatedAtDesc(user);
        for (Document doc : documents) {
            chunkRepository.deleteByDocument(doc);
            if (doc.getStoragePath() != null) {
                storageService.delete(doc.getStoragePath());
            }
        }
        documentRepository.deleteAll(documents);

        // 2. Delete Resumes & Physical Disk Files
        List<Resume> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user);
        for (Resume resume : resumes) {
            resumeAnalysisRepository.deleteByResume(resume);
            if (resume.getStoragePath() != null) {
                storageService.delete(resume.getStoragePath());
            }
        }
        resumeRepository.deleteAll(resumes);

        // 3. Delete Career Modules
        jobDescriptionAnalysisRepository.deleteAll(jobDescriptionAnalysisRepository.findByUserOrderByCreatedAtDesc(user));
        careerActionRepository.deleteAll(careerActionRepository.findByUserOrderBySuggestedDeadlineAscCreatedAtDesc(user));
        careerSkillRepository.deleteAll(careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user));

        // 4. Delete DSA Modules
        dsaAttemptRepository.deleteAll(dsaAttemptRepository.findByUserOrderByAttemptDateDescCreatedAtDesc(user));
        dsaRevisionRepository.deleteAll(dsaRevisionRepository.findByUserOrderByScheduledDateAsc(user));
        dsaProblemRepository.deleteAll(dsaProblemRepository.findByUserOrderByUpdatedAtDesc(user));

        // 5. Delete Knowledge Hub
        knowledgeItemRepository.deleteAll(knowledgeItemRepository.findByUserOrderByUpdatedAtDesc(user));

        // 6. Delete User Entity
        userRepository.delete(user);
        log.info("User account {} and all associated entities successfully deleted.", email);
    }

    private JobDescriptionAnalysisResponse toJobAnalysisResponse(JobDescriptionAnalysis analysis) {
        return JobDescriptionAnalysisResponse.fromEntity(
                analysis,
                fromJsonList(analysis.getResponsibilitiesJson()),
                fromJsonList(analysis.getMatchedRequiredSkillsJson()),
                fromJsonList(analysis.getMissingRequiredSkillsJson()),
                fromJsonList(analysis.getMatchedPreferredSkillsJson()),
                fromJsonList(analysis.getMissingPreferredSkillsJson()),
                fromJsonList(analysis.getKeywordCoverageJson()),
                fromJsonList(analysis.getTailoredSuggestionsJson())
        );
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
