package com.aischolartwin.service;

import com.aischolartwin.dto.JobDescriptionAnalysisRequest;
import com.aischolartwin.dto.JobDescriptionAnalysisResponse;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.JobDescriptionAnalysis;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.JobDescriptionAnalysisRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobDescriptionService {

    private final JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository;
    private final ResumeRepository resumeRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final ResumeExtractionService resumeExtractionService;
    private final ObjectMapper objectMapper;

    private static final Pattern EXP_PATTERN = Pattern.compile(
            "(?:\\b(?:\\d+\\+?\\s*(?:-|to)?\\s*\\d*\\s*(?:years?|yrs?)|entry[\\s-]level|freshers?|new[\\s-]grad)\\b(?:\\s*of\\s*experience)?)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EDU_PATTERN = Pattern.compile(
            "(?:\\b(?:Bachelor'?s?|Master'?s?|B\\.?Tech|B\\.S\\.?|B\\.E\\.?|M\\.?Tech|M\\.S\\.?|Degree|Diploma|Ph\\.?D)\\b(?:\\s*(?:in|of)?\\s*[a-zA-Z\\s]+)?)",
            Pattern.CASE_INSENSITIVE
    );

    public JobDescriptionService(JobDescriptionAnalysisRepository jobDescriptionAnalysisRepository,
                                 ResumeRepository resumeRepository,
                                 CareerSkillRepository careerSkillRepository,
                                 ResumeExtractionService resumeExtractionService,
                                 ObjectMapper objectMapper) {
        this.jobDescriptionAnalysisRepository = jobDescriptionAnalysisRepository;
        this.resumeRepository = resumeRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.resumeExtractionService = resumeExtractionService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobDescriptionAnalysisResponse analyzeJobDescription(User user, JobDescriptionAnalysisRequest request) {
        if (request.rawText() == null || request.rawText().isBlank()) {
            throw new InvalidPayloadException("Job description text cannot be empty.");
        }

        String rawText = request.rawText().trim();
        String title = request.jobTitle() != null && !request.jobTitle().isBlank()
                ? request.jobTitle().trim()
                : extractTitle(rawText);

        String company = request.companyName() != null && !request.companyName().isBlank()
                ? request.companyName().trim()
                : null;

        Resume targetResume = null;
        if (request.resumeId() != null) {
            targetResume = resumeRepository.findByIdAndUser(request.resumeId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + request.resumeId()));
        }

        // Extract Candidate Verified Skills from inventory
        List<CareerSkill> userSkills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        Set<String> candidateSkills = new HashSet<>();
        for (CareerSkill s : userSkills) {
            candidateSkills.add(s.getSkillName().toLowerCase());
        }

        // If target resume specified, include its extracted skills
        if (targetResume != null && targetResume.getSkillsJson() != null) {
            try {
                List<String> resumeSkills = objectMapper.readValue(targetResume.getSkillsJson(), new TypeReference<List<String>>() {});
                for (String rs : resumeSkills) {
                    candidateSkills.add(rs.toLowerCase());
                }
            } catch (Exception ignored) {}
        } else if (targetResume == null) {
            // Also include primary resume skills if available to provide the most comprehensive match
            resumeRepository.findByUserAndIsPrimaryTrue(user).ifPresent(primary -> {
                if (primary.getSkillsJson() != null) {
                    try {
                        List<String> rSkills = objectMapper.readValue(primary.getSkillsJson(), new TypeReference<List<String>>() {});
                        for (String rs : rSkills) {
                            candidateSkills.add(rs.toLowerCase());
                        }
                    } catch (Exception ignored) {}
                }
            });
        }

        // Extract skills from JD text
        List<String> jdDetectedSkills = resumeExtractionService.extractSkills(rawText);

        // Partition into required vs preferred based on contextual cues
        List<String> requiredSkills = new ArrayList<>();
        List<String> preferredSkills = new ArrayList<>();

        String lowerText = rawText.toLowerCase();
        int preferredIdx = -1;
        String[] preferredMarkers = new String[]{
                "preferred qualification", "preferred requirement", "preferred skill",
                "nice to have", "good to have", "bonus point", "bonus qualification", "plus:"
        };
        for (String marker : preferredMarkers) {
            int idx = lowerText.indexOf(marker);
            if (idx != -1 && (preferredIdx == -1 || idx < preferredIdx)) {
                preferredIdx = idx;
            }
        }

        for (String skill : jdDetectedSkills) {
            int skillIdx = lowerText.indexOf(skill.toLowerCase());
            if (preferredIdx != -1 && skillIdx >= preferredIdx) {
                if (!preferredSkills.contains(skill)) preferredSkills.add(skill);
            } else {
                if (!requiredSkills.contains(skill)) requiredSkills.add(skill);
            }
        }

        // If no preferred split found, default first 70% to required and rest to preferred
        if (preferredSkills.isEmpty() && requiredSkills.size() > 4) {
            int splitPoint = (int) Math.ceil(requiredSkills.size() * 0.7);
            preferredSkills = new ArrayList<>(requiredSkills.subList(splitPoint, requiredSkills.size()));
            requiredSkills = new ArrayList<>(requiredSkills.subList(0, splitPoint));
        }

        // Match calculation
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedPreferred = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();

        for (String req : requiredSkills) {
            if (candidateSkills.contains(req.toLowerCase())) {
                matchedRequired.add(req);
            } else {
                missingRequired.add(req);
            }
        }

        for (String pref : preferredSkills) {
            if (candidateSkills.contains(pref.toLowerCase())) {
                matchedPreferred.add(pref);
            } else {
                missingPreferred.add(pref);
            }
        }

        double reqWeight = requiredSkills.isEmpty() ? 75.0 : ((double) matchedRequired.size() / requiredSkills.size()) * 75.0;
        double prefWeight = preferredSkills.isEmpty() ? 25.0 : ((double) matchedPreferred.size() / preferredSkills.size()) * 25.0;
        double overallMatch = Math.round((reqWeight + prefWeight) * 10.0) / 10.0;

        // Extract Experience and Education requirements
        String expReq = extractRegex(rawText, EXP_PATTERN);
        String eduReq = extractRegex(rawText, EDU_PATTERN);

        // Extract Key Responsibilities
        List<String> responsibilities = extractResponsibilities(rawText);

        // Missing keywords & domain terminology
        List<String> missingKeywords = new ArrayList<>(missingRequired);
        for (String mp : missingPreferred) {
            if (!missingKeywords.contains(mp)) {
                missingKeywords.add(mp);
            }
        }

        // Tailored Suggestions
        List<String> suggestions = new ArrayList<>();
        if (!missingRequired.isEmpty()) {
            suggestions.add(String.format("Focus on acquiring or emphasizing core required technologies: %s.",
                    String.join(", ", missingRequired.subList(0, Math.min(3, missingRequired.size())))));
        }
        if (!matchedRequired.isEmpty()) {
            suggestions.add(String.format("Prominently highlight your experience with %s in your top resume project bullet points.",
                    String.join(", ", matchedRequired.subList(0, Math.min(3, matchedRequired.size())))));
        }
        if (!missingPreferred.isEmpty()) {
            suggestions.add(String.format("Nice-to-have bonus: Familiarize yourself with %s before technical interview rounds.",
                    String.join(", ", missingPreferred.subList(0, Math.min(2, missingPreferred.size())))));
        }
        suggestions.add("Ensure your resume bullets mirror the action verbs used in this job description's responsibilities.");

        JobDescriptionAnalysis analysis = new JobDescriptionAnalysis(
                user,
                targetResume,
                title,
                company,
                rawText,
                overallMatch
        );

        analysis.setExperienceRequired(expReq);
        analysis.setEducationRequired(eduReq);
        analysis.setResponsibilitiesJson(resumeExtractionService.toJson(responsibilities));
        analysis.setMatchedRequiredSkillsJson(resumeExtractionService.toJson(matchedRequired));
        analysis.setMissingRequiredSkillsJson(resumeExtractionService.toJson(missingRequired));
        analysis.setMatchedPreferredSkillsJson(resumeExtractionService.toJson(matchedPreferred));
        analysis.setMissingPreferredSkillsJson(resumeExtractionService.toJson(missingPreferred));
        analysis.setKeywordCoverageJson(resumeExtractionService.toJson(missingKeywords));
        analysis.setTailoredSuggestionsJson(resumeExtractionService.toJson(suggestions));

        JobDescriptionAnalysis saved = jobDescriptionAnalysisRepository.save(analysis);

        return JobDescriptionAnalysisResponse.fromEntity(
                saved,
                responsibilities,
                matchedRequired,
                missingRequired,
                matchedPreferred,
                missingPreferred,
                missingKeywords,
                suggestions
        );
    }

    @Transactional(readOnly = true)
    public List<JobDescriptionAnalysisResponse> getUserJobAnalyses(User user) {
        return jobDescriptionAnalysisRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobDescriptionAnalysisResponse getJobAnalysisById(User user, Long id) {
        JobDescriptionAnalysis analysis = jobDescriptionAnalysisRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job description analysis not found with ID: " + id));
        return toResponse(analysis);
    }

    private JobDescriptionAnalysisResponse toResponse(JobDescriptionAnalysis analysis) {
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

    private String extractTitle(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isBlank() || line.length() < 3) continue;
            if (line.matches("(?i).*(Engineer|Developer|Analyst|Scientist|Architect|Specialist|Consultant|Intern).*")) {
                return line.length() > 80 ? line.substring(0, 80) : line;
            }
        }
        return "Software Engineer Role";
    }

    private String extractRegex(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        return null;
    }

    private List<String> extractResponsibilities(String text) {
        List<String> list = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inRespSection = false;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.matches("(?i).*(responsibilities|what you'll do|duties|your role).*")) {
                inRespSection = true;
                continue;
            }
            if (inRespSection) {
                if (line.matches("(?i).*(requirements|qualifications|skills|benefits).*")) {
                    break;
                }
                if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                    list.add(line.replaceFirst("^[•\\-*]\\s*", ""));
                }
            }
        }
        return list;
    }

    @Transactional
    public void deleteJobAnalysis(User user, Long id) {
        JobDescriptionAnalysis analysis = jobDescriptionAnalysisRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Job description analysis with ID " + id + " not found."));
        jobDescriptionAnalysisRepository.delete(analysis);
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}

