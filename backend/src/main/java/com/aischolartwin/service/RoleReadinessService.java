package com.aischolartwin.service;

import com.aischolartwin.dto.RoleReadinessDto;
import com.aischolartwin.dto.SkillGapAnalysisResponse;
import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.SkillSource;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.DSAProblemRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoleReadinessService {

    private final CareerSkillRepository careerSkillRepository;
    private final DSAProblemRepository dsaProblemRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    public record RoleDefinition(
            String id,
            String name,
            List<String> requiredSkills,
            List<String> preferredSkills,
            boolean requiresDsaEvidence
    ) {}

    public static final List<RoleDefinition> BENCHMARK_ROLES = List.of(
            new RoleDefinition(
                    "sde",
                    "Software Development Engineer",
                    List.of("Java", "Data Structures", "Algorithms", "SQL", "Git", "Object-Oriented Programming"),
                    List.of("System Design", "Spring Boot", "Docker", "REST", "CI/CD"),
                    true
            ),
            new RoleDefinition(
                    "java-developer",
                    "Java Developer",
                    List.of("Java", "Spring Boot", "Hibernate", "SQL", "REST", "Git"),
                    List.of("Microservices", "Docker", "Maven", "JUnit", "Redis"),
                    true
            ),
            new RoleDefinition(
                    "full-stack",
                    "Full Stack Developer",
                    List.of("JavaScript", "React", "Node.js", "HTML", "CSS", "SQL", "Git"),
                    List.of("TypeScript", "Tailwind CSS", "Spring Boot", "Docker", "Next.js", "MongoDB"),
                    false
            ),
            new RoleDefinition(
                    "frontend-developer",
                    "Frontend Developer",
                    List.of("HTML", "CSS", "JavaScript", "React", "Git"),
                    List.of("TypeScript", "Tailwind CSS", "Redux", "Next.js", "Vite"),
                    false
            ),
            new RoleDefinition(
                    "backend-developer",
                    "Backend Developer",
                    List.of("Java", "Spring Boot", "SQL", "REST", "Database Management Systems", "Git"),
                    List.of("Microservices", "Docker", "Redis", "Kafka", "System Design"),
                    true
            ),
            new RoleDefinition(
                    "ai-ml-engineer",
                    "AI/ML Engineer",
                    List.of("Python", "Machine Learning", "PyTorch", "Pandas", "NumPy", "Git"),
                    List.of("Deep Learning", "TensorFlow", "Large Language Models", "RAG", "Scikit-Learn"),
                    false
            ),
            new RoleDefinition(
                    "data-analyst",
                    "Data Analyst",
                    List.of("SQL", "Python", "Data Structures", "Pandas", "NumPy"),
                    List.of("Machine Learning", "PostgreSQL", "R", "Git"),
                    false
            ),
            new RoleDefinition(
                    "devops-engineer",
                    "DevOps Engineer",
                    List.of("Linux", "Docker", "CI/CD", "Git", "Bash"),
                    List.of("Kubernetes", "AWS", "Terraform", "Jenkins", "Ansible"),
                    false
            )
    );

    public RoleReadinessService(CareerSkillRepository careerSkillRepository,
                                DSAProblemRepository dsaProblemRepository,
                                ResumeRepository resumeRepository,
                                ObjectMapper objectMapper) {
        this.careerSkillRepository = careerSkillRepository;
        this.dsaProblemRepository = dsaProblemRepository;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<RoleDefinition> getAvailableRoles() {
        return BENCHMARK_ROLES;
    }

    @Transactional(readOnly = true)
    public RoleReadinessDto evaluateRoleReadiness(User user, String roleId) {
        RoleDefinition role = BENCHMARK_ROLES.stream()
                .filter(r -> r.id().equalsIgnoreCase(roleId))
                .findFirst()
                .orElse(BENCHMARK_ROLES.get(0)); // Default to SDE

        List<CareerSkill> userSkills = careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user);
        Set<String> verifiedSkillNames = new HashSet<>();
        Map<String, CareerSkill> skillMap = new HashMap<>();

        Set<String> resumeSkillsSet = new LinkedHashSet<>();
        Set<String> verifiedSkillsSet = new LinkedHashSet<>();
        Set<String> inferredSkillsSet = new LinkedHashSet<>();

        for (CareerSkill s : userSkills) {
            String lower = s.getSkillName().toLowerCase();
            verifiedSkillNames.add(lower);
            skillMap.put(lower, s);
            verifiedSkillsSet.add(s.getSkillName());
            if (s.getSource() == SkillSource.RESUME) {
                resumeSkillsSet.add(s.getSkillName());
            } else if (s.getSource() == SkillSource.PROJECT) {
                inferredSkillsSet.add(s.getSkillName());
            }
        }

        // Also check skills from the user's latest/primary resume
        Optional<Resume> primaryResume = resumeRepository.findByUserAndIsPrimaryTrue(user)
                .or(() -> resumeRepository.findByUserOrderByCreatedAtDesc(user).stream().findFirst());
        if (primaryResume.isPresent() && primaryResume.get().getSkillsJson() != null) {
            try {
                List<String> rSkills = objectMapper.readValue(primaryResume.get().getSkillsJson(), new TypeReference<List<String>>() {});
                for (String rs : rSkills) {
                    resumeSkillsSet.add(rs);
                    verifiedSkillNames.add(rs.toLowerCase());
                }
            } catch (Exception ignored) {}
        }

        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedPreferred = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();
        List<String> supportingEvidence = new ArrayList<>();

        for (String req : role.requiredSkills()) {
            if (hasSkillMatch(verifiedSkillNames, req)) {
                matchedRequired.add(req);
                CareerSkill skill = getMatchedSkill(skillMap, req);
                if (skill != null && skill.getEvidenceText() != null) {
                    supportingEvidence.add(req + ": " + skill.getEvidenceText());
                } else {
                    supportingEvidence.add(req + ": Verified in skills inventory (" + (skill != null ? skill.getProficiency() : "INTERMEDIATE") + ")");
                }
            } else {
                missingRequired.add(req);
            }
        }

        for (String pref : role.preferredSkills()) {
            if (hasSkillMatch(verifiedSkillNames, pref)) {
                matchedPreferred.add(pref);
                CareerSkill skill = getMatchedSkill(skillMap, pref);
                if (skill != null && skill.getEvidenceText() != null) {
                    supportingEvidence.add(pref + ": " + skill.getEvidenceText());
                }
            } else {
                missingPreferred.add(pref);
            }
        }

        double requiredScore = role.requiredSkills().isEmpty() ? 70.0 :
                ((double) matchedRequired.size() / role.requiredSkills().size()) * 70.0;
        double preferredScore = role.preferredSkills().isEmpty() ? 30.0 :
                ((double) matchedPreferred.size() / role.preferredSkills().size()) * 30.0;
        double readinessPercentage = Math.round((requiredScore + preferredScore) * 10.0) / 10.0;

        // Algorithmic DSA Evidence integration & skill inference
        String dsaEvidence = null;
        long solvedDsaCount = dsaProblemRepository.countByUserAndStatus(user, DSAStatus.SOLVED);
        if (solvedDsaCount > 0) {
            inferredSkillsSet.add("Data Structures");
            inferredSkillsSet.add("Algorithms");
            dsaEvidence = String.format("Verified %d solved DSA coding problems in ScholarTwin DSA Practice Tracker.", solvedDsaCount);
            if (role.requiresDsaEvidence()) {
                supportingEvidence.add("Algorithmic Verification: " + dsaEvidence);
            }
        } else if (role.requiresDsaEvidence()) {
            dsaEvidence = "No solved problems in ScholarTwin DSA Tracker yet. Practice in DSA Tracker to strengthen your coding evidence.";
        }

        List<String> recommendedSkillsToLearn = new ArrayList<>(missingRequired);
        recommendedSkillsToLearn.addAll(missingPreferred.subList(0, Math.min(2, missingPreferred.size())));

        List<String> allMissingSkills = new ArrayList<>(missingRequired);
        for (String mp : missingPreferred) {
            if (!allMissingSkills.contains(mp)) {
                allMissingSkills.add(mp);
            }
        }

        return new RoleReadinessDto(
                role.id(),
                role.name(),
                readinessPercentage,
                matchedRequired.size(),
                role.requiredSkills().size(),
                matchedPreferred.size(),
                role.preferredSkills().size(),
                matchedRequired,
                missingRequired,
                matchedPreferred,
                missingPreferred,
                recommendedSkillsToLearn,
                supportingEvidence,
                dsaEvidence,
                new ArrayList<>(resumeSkillsSet),
                new ArrayList<>(verifiedSkillsSet),
                new ArrayList<>(inferredSkillsSet),
                allMissingSkills
        );
    }

    @Transactional(readOnly = true)
    public SkillGapAnalysisResponse getSkillGapAnalysis(User user, String targetRoleId) {
        String activeRoleId = (targetRoleId != null && !targetRoleId.isBlank()) ? targetRoleId : "sde";
        RoleReadinessDto targetRole = evaluateRoleReadiness(user, activeRoleId);

        List<RoleReadinessDto> allRoles = new ArrayList<>();
        Map<String, Integer> missingSkillFrequency = new HashMap<>();

        for (RoleDefinition roleDef : BENCHMARK_ROLES) {
            RoleReadinessDto readiness = evaluateRoleReadiness(user, roleDef.id());
            allRoles.add(readiness);

            for (String missing : readiness.missingRequiredSkills()) {
                missingSkillFrequency.put(missing, missingSkillFrequency.getOrDefault(missing, 0) + 1);
            }
        }

        List<String> topMissingAcrossRoles = missingSkillFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        long totalVerifiedSkills = careerSkillRepository.countByUser(user);

        return new SkillGapAnalysisResponse(
                targetRole,
                allRoles,
                totalVerifiedSkills,
                topMissingAcrossRoles
        );
    }

    private boolean hasSkillMatch(Set<String> verifiedSkillNames, String skillName) {
        String lower = skillName.toLowerCase();
        if (verifiedSkillNames.contains(lower)) return true;
        // Aliases
        if (lower.equals("react") && verifiedSkillNames.contains("react.js")) return true;
        if (lower.equals("node.js") && (verifiedSkillNames.contains("node") || verifiedSkillNames.contains("nodejs"))) return true;
        if (lower.equals("c++") && verifiedSkillNames.contains("cpp")) return true;
        if (lower.equals("golang") && verifiedSkillNames.contains("go")) return true;
        if (lower.equals("algorithms") && verifiedSkillNames.contains("data structures")) return true;
        return false;
    }

    private CareerSkill getMatchedSkill(Map<String, CareerSkill> skillMap, String skillName) {
        String lower = skillName.toLowerCase();
        if (skillMap.containsKey(lower)) return skillMap.get(lower);
        if (lower.equals("react") && skillMap.containsKey("react.js")) return skillMap.get("react.js");
        if (lower.equals("node.js") && skillMap.containsKey("node")) return skillMap.get("node");
        return null;
    }
}

