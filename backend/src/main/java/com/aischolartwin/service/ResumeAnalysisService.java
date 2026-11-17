package com.aischolartwin.service;

import com.aischolartwin.dto.DimensionScoreDto;
import com.aischolartwin.dto.ExtractedResumeDataDto;
import com.aischolartwin.dto.ResumeAnalysisResponse;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.ResumeAnalysis;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.ResumeAnalysisRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeAnalysisService {

    public static final String SCORING_METHOD = "SCHOLARTWIN_EXPLAINABLE_RUBRIC_V1";

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ResumeExtractionService resumeExtractionService;
    private final ObjectMapper objectMapper;

    // Metric Detection Pattern for Measurable Results
    private static final Pattern METRIC_PATTERN = Pattern.compile(
            "(?:\\b\\d+%(?:\\s*(?:increase|decrease|reduction|improvement|growth|boost|faster))?|\\$\\d+[kKmM]?|\\d+[kKmM]\\+?\\s*(?:users|requests|queries|downloads|records)|reduced\\s+(?:latency|time|cost)?\\s*by\\s*\\d+|improved\\s+(?:performance|speed)?\\s*by\\s*\\d+|\\b\\d+x\\b|sub-\\d+ms|\\b\\d+\\+?\\s*(?:stars|forks|clients))",
            Pattern.CASE_INSENSITIVE
    );

    public ResumeAnalysisService(ResumeAnalysisRepository resumeAnalysisRepository,
                                 ResumeExtractionService resumeExtractionService,
                                 ObjectMapper objectMapper) {
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.resumeExtractionService = resumeExtractionService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResumeAnalysis analyzeResume(Resume resume) {
        User user = resume.getUser();
        String rawText = resume.getExtractedText() != null ? resume.getExtractedText() : "";
        ExtractedResumeDataDto extracted = resumeExtractionService.parseStructuredData(rawText);

        // 1. Technical Skills Coverage (Max 20)
        List<String> skills = extracted.skills() != null ? extracted.skills() : List.of();
        Set<String> distinctCategories = new HashSet<>();
        for (String skill : skills) {
            var cat = ResumeExtractionService.KNOWN_SKILLS.get(skill);
            if (cat != null) distinctCategories.add(cat.name());
        }

        int skillsScore;
        String skillsStatus;
        String skillsEvidence = String.format("%d technical skills identified across %d categories: %s",
                skills.size(), distinctCategories.size(), String.join(", ", skills.subList(0, Math.min(6, skills.size()))));
        if (skills.size() >= 8 && distinctCategories.size() >= 3) {
            skillsScore = 20;
            skillsStatus = "EXCELLENT";
        } else if (skills.size() >= 5 && distinctCategories.size() >= 2) {
            skillsScore = 16;
            skillsStatus = "GOOD";
        } else if (skills.size() >= 3) {
            skillsScore = 12;
            skillsStatus = "NEEDS_IMPROVEMENT";
        } else if (!skills.isEmpty()) {
            skillsScore = 7;
            skillsStatus = "CRITICAL";
        } else {
            skillsScore = 0;
            skillsStatus = "CRITICAL";
            skillsEvidence = "No recognizable technical skills identified in resume text.";
        }

        // 2. Project Depth & Technical Scope (Max 20)
        int projectsCount = extracted.projects() != null ? extracted.projects().size() : 0;
        int projectsScore;
        String projectsStatus;
        String projectsEvidence;
        if (projectsCount >= 2) {
            projectsScore = 20;
            projectsStatus = "EXCELLENT";
            projectsEvidence = String.format("%d technical projects documented with implementation scope.", projectsCount);
        } else if (projectsCount == 1) {
            projectsScore = 14;
            projectsStatus = "GOOD";
            projectsEvidence = "1 technical project found. Recommended minimum for competitive roles is 2-3 projects.";
        } else if (rawText.toLowerCase().contains("project")) {
            projectsScore = 8;
            projectsStatus = "NEEDS_IMPROVEMENT";
            projectsEvidence = "Project mentions detected but missing structured titles or bullet points.";
        } else {
            projectsScore = 0;
            projectsStatus = "CRITICAL";
            projectsEvidence = "No projects section detected.";
        }

        // 3. Experience & Internships (Max 15)
        int expCount = extracted.experience() != null ? extracted.experience().size() : 0;
        int expScore;
        String expStatus;
        String expEvidence;
        if (expCount >= 2) {
            expScore = 15;
            expStatus = "EXCELLENT";
            expEvidence = String.format("%d professional work/internship experiences documented.", expCount);
        } else if (expCount == 1) {
            expScore = 12;
            expStatus = "GOOD";
            expEvidence = "1 work/internship experience documented.";
        } else if (projectsCount >= 2) {
            expScore = 8; // Student/entry-level credit for strong projects
            expStatus = "NEEDS_IMPROVEMENT";
            expEvidence = "No formal employment listed; academic and personal projects provide baseline engineering evidence.";
        } else {
            expScore = 2;
            expStatus = "CRITICAL";
            expEvidence = "No formal work experience or internships found.";
        }

        // 4. Measurable Outcomes & Quantifiable Results (Max 15)
        List<String> matchedMetrics = new ArrayList<>();
        Matcher metricMatcher = METRIC_PATTERN.matcher(rawText);
        while (metricMatcher.find() && matchedMetrics.size() < 6) {
            matchedMetrics.add(metricMatcher.group(0).trim());
        }

        int metricsScore;
        String metricsStatus;
        String metricsEvidence;
        if (matchedMetrics.size() >= 3) {
            metricsScore = 15;
            metricsStatus = "EXCELLENT";
            metricsEvidence = String.format("Found %d quantifiable impact metrics (e.g. %s).",
                    matchedMetrics.size(), String.join(", ", matchedMetrics.subList(0, Math.min(3, matchedMetrics.size()))));
        } else if (matchedMetrics.size() >= 1) {
            metricsScore = 10;
            metricsStatus = "GOOD";
            metricsEvidence = String.format("Found %d metric: %s. More metrics recommended for high-impact bullets.",
                    matchedMetrics.size(), matchedMetrics.get(0));
        } else {
            metricsScore = 3;
            metricsStatus = "NEEDS_IMPROVEMENT";
            metricsEvidence = "Zero quantifiable metrics found. Adding numbers (%, scale, latency, users) significantly elevates impact.";
        }

        // 5. Certifications & Accreditations (Max 10)
        int certsCount = extracted.certifications() != null ? extracted.certifications().size() : 0;
        int certsScore;
        String certsStatus;
        String certsEvidence;
        if (certsCount >= 2) {
            certsScore = 10;
            certsStatus = "EXCELLENT";
            certsEvidence = String.format("%d industry certifications/credentials verified.", certsCount);
        } else if (certsCount == 1) {
            certsScore = 7;
            certsStatus = "GOOD";
            certsEvidence = "1 certification verified.";
        } else {
            certsScore = 3;
            certsStatus = "NEEDS_IMPROVEMENT";
            certsEvidence = "No certifications or accredited courses listed.";
        }

        // 6. Structure, Formatting & Readability (Max 10)
        boolean hasEmail = extracted.email() != null;
        boolean hasPhone = extracted.phone() != null;
        boolean hasGithub = extracted.githubUrl() != null;
        boolean hasLinkedin = extracted.linkedinUrl() != null;

        int structScore = 10;
        List<String> missingContacts = new ArrayList<>();
        if (!hasEmail) { structScore -= 3; missingContacts.add("email"); }
        if (!hasPhone) { structScore -= 2; missingContacts.add("phone"); }
        if (!hasGithub) { structScore -= 2; missingContacts.add("GitHub profile"); }
        if (!hasLinkedin) { structScore -= 2; missingContacts.add("LinkedIn profile"); }
        structScore = Math.max(3, structScore);

        String structStatus = structScore >= 9 ? "EXCELLENT" : (structScore >= 7 ? "GOOD" : "NEEDS_IMPROVEMENT");
        String structEvidence = missingContacts.isEmpty()
                ? "All essential contact details and standard sections detected."
                : "Missing contact items: " + String.join(", ", missingContacts);

        // 7. Modern Tooling & Role Relevance (Max 10)
        Set<String> modernKeywords = Set.of("Git", "Docker", "REST", "CI/CD", "Linux", "Kubernetes", "AWS", "JUnit");
        long modernCount = skills.stream().filter(modernKeywords::contains).count();
        int relevanceScore;
        String relevanceStatus;
        String relevanceEvidence;
        if (modernCount >= 4) {
            relevanceScore = 10;
            relevanceStatus = "EXCELLENT";
            relevanceEvidence = String.format("Includes %d industry-standard workflow keywords (Git, Docker, CI/CD, etc.).", modernCount);
        } else if (modernCount >= 2) {
            relevanceScore = 7;
            relevanceStatus = "GOOD";
            relevanceEvidence = String.format("Includes %d modern workflow keywords.", modernCount);
        } else {
            relevanceScore = 4;
            relevanceStatus = "NEEDS_IMPROVEMENT";
            relevanceEvidence = "Few modern developer workflow keywords found (e.g. Git, Docker, CI/CD).";
        }

        // Total Overall Score (0-100)
        int overallScore = skillsScore + projectsScore + expScore + metricsScore + certsScore + structScore + relevanceScore;

        // Build Dimension Breakdown
        List<DimensionScoreDto> dimensions = List.of(
                new DimensionScoreDto("technical_skills", "Technical Skills Coverage", skillsScore, 20, (skillsScore / 20.0) * 100,
                        "Depth and breadth of recognized programming languages, frameworks, and databases.", skillsEvidence, skillsStatus),
                new DimensionScoreDto("projects_depth", "Project Descriptions & Scope", projectsScore, 20, (projectsScore / 20.0) * 100,
                        "Presence of substantial technical projects demonstrating practical implementation.", projectsEvidence, projectsStatus),
                new DimensionScoreDto("experience", "Work & Internship Experience", expScore, 15, (expScore / 15.0) * 100,
                        "Professional experience, internships, or industry collaboration.", expEvidence, expStatus),
                new DimensionScoreDto("measurable_results", "Measurable Engineering Impact", metricsScore, 15, (metricsScore / 15.0) * 100,
                        "Quantifiable outcomes, efficiency percentages, scale, or metrics in descriptions.", metricsEvidence, metricsStatus),
                new DimensionScoreDto("certifications", "Certifications & Credentials", certsScore, 10, (certsScore / 10.0) * 100,
                        "Accredited industry certifications, specializations, and verified courses.", certsEvidence, certsStatus),
                new DimensionScoreDto("structure_readability", "Structure & Contact Completeness", structScore, 10, (structScore / 10.0) * 100,
                        "Complete contact details (Email, Phone, GitHub, LinkedIn) and clean organization.", structEvidence, structStatus),
                new DimensionScoreDto("role_relevance", "Modern Tooling & Workflow Relevance", relevanceScore, 10, (relevanceScore / 10.0) * 100,
                        "Industry-standard toolchain terminology (Git, Docker, CI/CD, Cloud, Testing).", relevanceEvidence, relevanceStatus)
        );

        // Strengths & Weaknesses
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        List<String> missingInfo = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (skillsScore >= 16) strengths.add("Strong technical skills coverage with " + skills.size() + " technologies documented.");
        else weaknesses.add("Technical skills section could be expanded with core languages, databases, and frameworks.");

        if (projectsScore >= 16) strengths.add("Strong project portfolio demonstrating practical implementation.");
        else recommendations.add("Add at least 2 full-stack or systems projects with live demo or GitHub links.");

        if (metricsScore >= 12) strengths.add("Effective use of quantifiable engineering metrics in accomplishment bullets.");
        else {
            weaknesses.add("Descriptions lack measurable impact metrics (latency, percentages, scale, users).");
            recommendations.add("Quantify bullet points: state by how much you improved performance, scale handled, or time saved.");
        }

        if (certsScore >= 8) strengths.add("Recognized industry certifications validate technical competence.");

        if (!hasGithub) {
            missingInfo.add("GitHub profile link missing from resume header.");
            recommendations.add("Include a link to your active GitHub profile with pinned repositories.");
        }
        if (!hasLinkedin) {
            missingInfo.add("LinkedIn profile link missing from resume header.");
        }
        if (extracted.summary() == null || extracted.summary().isBlank()) {
            missingInfo.add("Professional summary or career profile statement missing.");
            recommendations.add("Add a concise 2-line summary highlighting your target role and key strengths.");
        }

        if (modernCount < 3) {
            recommendations.add("Highlight version control (Git), containerization (Docker), and CI/CD pipelines in your tooling stack.");
        }

        ResumeAnalysis analysis = new ResumeAnalysis(
                resume,
                user,
                overallScore,
                skillsScore,
                projectsScore,
                expScore,
                metricsScore,
                certsScore,
                structScore,
                relevanceScore
        );

        analysis.setStrengthsJson(resumeExtractionService.toJson(strengths));
        analysis.setWeaknessesJson(resumeExtractionService.toJson(weaknesses));
        analysis.setMissingInformationJson(resumeExtractionService.toJson(missingInfo));
        analysis.setActionableRecommendationsJson(resumeExtractionService.toJson(recommendations));
        analysis.setDimensionBreakdownJson(resumeExtractionService.toJson(dimensions));
        analysis.setScoringMethod(SCORING_METHOD);

        return resumeAnalysisRepository.save(analysis);
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getAnalysisResponse(ResumeAnalysis analysis) {
        if (analysis == null) return null;

        List<String> strengths = fromJsonList(analysis.getStrengthsJson());
        List<String> weaknesses = fromJsonList(analysis.getWeaknessesJson());
        List<String> missing = fromJsonList(analysis.getMissingInformationJson());
        List<String> recs = fromJsonList(analysis.getActionableRecommendationsJson());
        List<DimensionScoreDto> dimensions = fromJsonDimensionList(analysis.getDimensionBreakdownJson());

        return new ResumeAnalysisResponse(
                analysis.getId(),
                analysis.getResume().getId(),
                analysis.getResume().getTitle(),
                analysis.getOverallScore(),
                analysis.getTechnicalSkillsScore(),
                analysis.getProjectsDepthScore(),
                analysis.getExperienceScore(),
                analysis.getMeasurableResultsScore(),
                analysis.getCertificationsScore(),
                analysis.getStructureReadabilityScore(),
                analysis.getRoleRelevanceScore(),
                strengths,
                weaknesses,
                missing,
                recs,
                dimensions,
                analysis.getScoringMethod(),
                analysis.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getLatestAnalysisForResume(User user, Long resumeId) {
        return resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(a -> a.getResume().getId().equals(resumeId))
                .findFirst()
                .map(this::getAnalysisResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getAnalysisById(User user, Long analysisId) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with ID: " + analysisId));
        return getAnalysisResponse(analysis);
    }

    @Transactional(readOnly = true)
    public List<ResumeAnalysisResponse> getAnalysisHistory(User user) {
        return resumeAnalysisRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::getAnalysisResponse)
                .toList();
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<DimensionScoreDto> fromJsonDimensionList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<DimensionScoreDto>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}

