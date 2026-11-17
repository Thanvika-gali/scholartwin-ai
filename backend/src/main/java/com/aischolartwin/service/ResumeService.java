package com.aischolartwin.service;

import com.aischolartwin.dto.*;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.JobDescriptionAnalysisRepository;
import com.aischolartwin.repository.ResumeAnalysisRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.aischolartwin.service.extraction.ExtractionResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository;
    private final FileStorageService fileStorageService;
    private final ResumeFileValidatorService fileValidatorService;
    private final ResumeExtractionService resumeExtractionService;
    private final ResumeAnalysisService resumeAnalysisService;
    private final CareerSkillService careerSkillService;
    private final CareerActionService careerActionService;
    private final ObjectMapper objectMapper;

    public ResumeService(ResumeRepository resumeRepository,
                         ResumeAnalysisRepository resumeAnalysisRepository,
                         JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository,
                         FileStorageService fileStorageService,
                         ResumeFileValidatorService fileValidatorService,
                         ResumeExtractionService resumeExtractionService,
                         ResumeAnalysisService resumeAnalysisService,
                         CareerSkillService careerSkillService,
                         CareerActionService careerActionService,
                         ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobDescriptionAnalysisRepository = jobDescriptionAnalysisRepository;
        this.fileStorageService = fileStorageService;
        this.fileValidatorService = fileValidatorService;
        this.resumeExtractionService = resumeExtractionService;
        this.resumeAnalysisService = resumeAnalysisService;
        this.careerSkillService = careerSkillService;
        this.careerActionService = careerActionService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeDetailResponse uploadResume(User user, MultipartFile file, String customTitle) {
        ResumeFileType fileType = fileValidatorService.validateAndDetermineType(file);

        String originalFilename = file.getOriginalFilename();
        String extension = fileType == ResumeFileType.PDF ? "pdf" : "docx";
        String storagePath = fileStorageService.store(file, extension);

        // Determine version number
        Optional<Resume> latestResume = resumeRepository.findTopByUserOrderByVersionNumberDesc(user);
        int nextVersion = latestResume.map(r -> r.getVersionNumber() + 1).orElse(1);

        String title = (customTitle != null && !customTitle.isBlank())
                ? customTitle.trim()
                : (originalFilename != null ? originalFilename : "Resume v" + nextVersion);

        Resume resume = new Resume(
                user,
                title,
                originalFilename != null ? originalFilename : "resume." + extension,
                storagePath,
                fileType,
                file.getSize(),
                nextVersion
        );

        // First resume defaults to primary
        long existingCount = resumeRepository.countByUser(user);
        if (existingCount == 0) {
            resume.setPrimary(true);
        }

        resume.setProcessingStatus(ResumeProcessingStatus.PROCESSING);
        resume = resumeRepository.save(resume);

        // Text & Structured Field Extraction
        try {
            Path physicalPath = fileStorageService.resolve(storagePath);
            File diskFile = physicalPath.toFile();

            ExtractionResult extraction = resumeExtractionService.extractRawText(diskFile, fileType);
            String rawText = extraction.getFullText();
            resume.setExtractedText(rawText);

            ExtractedResumeDataDto structured = resumeExtractionService.parseStructuredData(rawText);
            resume.setFullName(structured.fullName());
            resume.setEmail(structured.email());
            resume.setPhone(structured.phone());
            resume.setLinkedinUrl(structured.linkedinUrl());
            resume.setGithubUrl(structured.githubUrl());
            resume.setPortfolioUrl(structured.portfolioUrl());
            resume.setSummary(structured.summary());

            resume.setSkillsJson(resumeExtractionService.toJson(structured.skills()));
            resume.setEducationJson(resumeExtractionService.toJson(structured.education()));
            resume.setExperienceJson(resumeExtractionService.toJson(structured.experience()));
            resume.setProjectsJson(resumeExtractionService.toJson(structured.projects()));
            resume.setCertificationsJson(resumeExtractionService.toJson(structured.certifications()));
            resume.setAchievementsJson(resumeExtractionService.toJson(structured.achievements()));

            resume.setProcessingStatus(ResumeProcessingStatus.PARSED);
            resume = resumeRepository.save(resume);

            // Automatic Analysis Generation
            ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resume);

            // Automatic Skill Import & Action Generation
            careerSkillService.importSkillsFromResume(user, resume.getId());
            careerActionService.generateRecommendedActionsFromAnalysis(user, analysis);

            ResumeAnalysisResponse analysisResponse = resumeAnalysisService.getAnalysisResponse(analysis);
            ResumeResponse resumeResponse = ResumeResponse.fromEntity(resume, true, analysis.getOverallScore());

            return new ResumeDetailResponse(resumeResponse, structured, rawText, analysisResponse, 1);
        } catch (Exception e) {
            log.error("Failed to extract or analyze resume: {}", e.getMessage(), e);
            resume.setProcessingStatus(ResumeProcessingStatus.FAILED);
            resume.setErrorMessage("Text extraction failed: " + e.getMessage());
            resume = resumeRepository.save(resume);

            ResumeResponse resumeResponse = ResumeResponse.fromEntity(resume, false, null);
            return new ResumeDetailResponse(resumeResponse, null, null, null, 0);
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(User user) {
        List<Resume> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user);
        List<ResumeResponse> list = new ArrayList<>();

        for (Resume r : resumes) {
            Optional<ResumeAnalysis> latestAnalysis = resumeAnalysisRepository.findTopByResumeOrderByCreatedAtDesc(r);
            boolean hasAnalysis = latestAnalysis.isPresent();
            Integer score = latestAnalysis.map(ResumeAnalysis::getOverallScore).orElse(null);
            list.add(ResumeResponse.fromEntity(r, hasAnalysis, score));
        }

        return list;
    }

    @Transactional(readOnly = true)
    public ResumeDetailResponse getResumeDetail(User user, Long id) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        Optional<ResumeAnalysis> latestAnalysis = resumeAnalysisRepository.findTopByResumeOrderByCreatedAtDesc(resume);
        boolean hasAnalysis = latestAnalysis.isPresent();
        Integer score = latestAnalysis.map(ResumeAnalysis::getOverallScore).orElse(null);
        ResumeResponse resumeResponse = ResumeResponse.fromEntity(resume, hasAnalysis, score);

        ExtractedResumeDataDto extracted = buildExtractedDto(resume);
        ResumeAnalysisResponse analysisResponse = latestAnalysis
                .map(resumeAnalysisService::getAnalysisResponse)
                .orElse(null);

        long historyCount = resumeAnalysisRepository.findByResumeOrderByCreatedAtDesc(resume).size();

        return new ResumeDetailResponse(
                resumeResponse,
                extracted,
                resume.getExtractedText(),
                analysisResponse,
                historyCount
        );
    }

    @Transactional
    public ResumeResponse setPrimaryResume(User user, Long id) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        resumeRepository.clearOtherPrimaryResumes(user, id);
        resume.setPrimary(true);
        Resume updated = resumeRepository.save(resume);

        Optional<ResumeAnalysis> analysis = resumeAnalysisRepository.findTopByResumeOrderByCreatedAtDesc(updated);
        return ResumeResponse.fromEntity(updated, analysis.isPresent(), analysis.map(ResumeAnalysis::getOverallScore).orElse(null));
    }

    @Transactional
    public ResumeDetailResponse updateResume(User user, Long id, ResumeUpdateRequest request) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        if (request.title() != null && !request.title().isBlank()) {
            resume.setTitle(request.title().trim());
        }
        if (request.fullName() != null) resume.setFullName(request.fullName().trim());
        if (request.email() != null) resume.setEmail(request.email().trim());
        if (request.phone() != null) resume.setPhone(request.phone().trim());
        if (request.linkedinUrl() != null) resume.setLinkedinUrl(request.linkedinUrl().trim());
        if (request.githubUrl() != null) resume.setGithubUrl(request.githubUrl().trim());
        if (request.portfolioUrl() != null) resume.setPortfolioUrl(request.portfolioUrl().trim());
        if (request.summary() != null) resume.setSummary(request.summary().trim());

        if (request.skills() != null) {
            resume.setSkillsJson(resumeExtractionService.toJson(request.skills()));
        }
        if (request.educationJson() != null) resume.setEducationJson(request.educationJson());
        if (request.experienceJson() != null) resume.setExperienceJson(request.experienceJson());
        if (request.projectsJson() != null) resume.setProjectsJson(request.projectsJson());
        if (request.certificationsJson() != null) resume.setCertificationsJson(request.certificationsJson());
        if (request.achievementsJson() != null) resume.setAchievementsJson(request.achievementsJson());

        Resume updated = resumeRepository.save(resume);

        // Re-analyze with edited details
        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(updated);
        careerSkillService.importSkillsFromResume(user, updated.getId());

        ResumeResponse resumeResponse = ResumeResponse.fromEntity(updated, true, analysis.getOverallScore());
        ExtractedResumeDataDto extracted = buildExtractedDto(updated);
        ResumeAnalysisResponse analysisResponse = resumeAnalysisService.getAnalysisResponse(analysis);

        return new ResumeDetailResponse(resumeResponse, extracted, updated.getExtractedText(), analysisResponse, 1);
    }

    @Transactional
    public void deleteResume(User user, Long id) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));

        boolean wasPrimary = resume.isPrimary();
        String storagePath = resume.getStoragePath();

        // Remove DB references
        jobDescriptionAnalysisRepository.deleteByResume(resume);
        resumeAnalysisRepository.deleteByResume(resume);
        resumeRepository.delete(resume);

        // Delete physical file on disk
        try {
            fileStorageService.delete(storagePath);
        } catch (Exception e) {
            log.warn("Could not delete physical resume file: {}", e.getMessage());
        }

        // If deleted resume was primary, designate the next newest resume as primary
        if (wasPrimary) {
            Optional<Resume> fallback = resumeRepository.findByUserOrderByCreatedAtDesc(user).stream().findFirst();
            if (fallback.isPresent()) {
                Resume nextPrimary = fallback.get();
                nextPrimary.setPrimary(true);
                resumeRepository.save(nextPrimary);
            }
        }
    }

    @Transactional(readOnly = true)
    public Resource downloadResume(User user, Long id) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));
        return fileStorageService.loadAsResource(resume.getStoragePath());
    }

    @Transactional(readOnly = true)
    public Resume getResumeEntity(User user, Long id) {
        return resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));
    }

    private ExtractedResumeDataDto buildExtractedDto(Resume r) {
        List<String> skills = fromJson(r.getSkillsJson(), new TypeReference<List<String>>() {});
        List<ExtractedResumeDataDto.EducationItemDto> education = fromJson(r.getEducationJson(), new TypeReference<List<ExtractedResumeDataDto.EducationItemDto>>() {});
        List<ExtractedResumeDataDto.ExperienceItemDto> experience = fromJson(r.getExperienceJson(), new TypeReference<List<ExtractedResumeDataDto.ExperienceItemDto>>() {});
        List<ExtractedResumeDataDto.ProjectItemDto> projects = fromJson(r.getProjectsJson(), new TypeReference<List<ExtractedResumeDataDto.ProjectItemDto>>() {});
        List<ExtractedResumeDataDto.CertificationItemDto> certs = fromJson(r.getCertificationsJson(), new TypeReference<List<ExtractedResumeDataDto.CertificationItemDto>>() {});
        List<String> achievements = fromJson(r.getAchievementsJson(), new TypeReference<List<String>>() {});

        return new ExtractedResumeDataDto(
                r.getFullName(),
                r.getEmail(),
                r.getPhone(),
                r.getLinkedinUrl(),
                r.getGithubUrl(),
                r.getPortfolioUrl(),
                r.getSummary(),
                skills != null ? skills : List.of(),
                education != null ? education : List.of(),
                experience != null ? experience : List.of(),
                projects != null ? projects : List.of(),
                certs != null ? certs : List.of(),
                achievements != null ? achievements : List.of()
        );
    }

    private <T> T fromJson(String json, TypeReference<T> ref) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, ref);
        } catch (Exception e) {
            return null;
        }
    }
}
