package com.aischolartwin.service;

import com.aischolartwin.dto.ExtractedResumeDataDto;
import com.aischolartwin.dto.ExtractedResumeDataDto.*;
import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.ResumeFileType;
import com.aischolartwin.service.extraction.DocxExtractionService;
import com.aischolartwin.service.extraction.ExtractionResult;
import com.aischolartwin.service.extraction.PdfExtractionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ResumeExtractionService.class);

    private final PdfExtractionService pdfExtractionService;
    private final DocxExtractionService docxExtractionService;
    private final ObjectMapper objectMapper;

    // RegEx Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE);

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");

    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?linkedin\\.com/in/([a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GITHUB_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?github\\.com/([a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PORTFOLIO_PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?[a-zA-Z0-9.-]+\\.(?:com|io|me|dev|app|org)(?:/[a-zA-Z0-9_.-]+)*", Pattern.CASE_INSENSITIVE);

    // Dictionary of Technical Skills & their default Category
    public static final Map<String, CareerSkillCategory> KNOWN_SKILLS = new LinkedHashMap<>();

    static {
        // Programming Languages
        List.of("Java", "Python", "C++", "C#", "C", "JavaScript", "TypeScript", "Go", "Golang", "Rust",
                "Kotlin", "Swift", "PHP", "Ruby", "SQL", "HTML", "CSS", "Bash", "Shell", "R", "Scala")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.PROGRAMMING_LANGUAGES));

        // Frontend
        List.of("React", "React.js", "Angular", "Vue", "Vue.js", "Next.js", "Redux", "Tailwind CSS",
                "Tailwind", "Bootstrap", "Vite", "Webpack", "SASS", "SCSS", "jQuery", "HTML5", "CSS3")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.FRONTEND));

        // Backend
        List.of("Spring Boot", "Spring", "Hibernate", "JPA", "Node.js", "Express", "Express.js",
                "Django", "Flask", "FastAPI", "ASP.NET", "Ruby on Rails", "REST", "REST APIs",
                "GraphQL", "Microservices", "gRPC", "WebSockets")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.BACKEND));

        // Databases
        List.of("MySQL", "PostgreSQL", "MongoDB", "Redis", "SQLite", "Oracle", "Cassandra",
                "Elasticsearch", "DynamoDB", "MariaDB", "Firebase", "Neo4j")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.DATABASES));

        // Cloud & DevOps
        List.of("AWS", "Amazon Web Services", "GCP", "Google Cloud", "Azure", "Docker", "Kubernetes",
                "CI/CD", "GitHub Actions", "Jenkins", "Terraform", "Ansible", "Linux", "Nginx", "GitLab CI")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.CLOUD_DEVOPS));

        // AI & ML
        List.of("Machine Learning", "Deep Learning", "PyTorch", "TensorFlow", "Scikit-Learn", "NumPy",
                "Pandas", "Keras", "NLP", "Natural Language Processing", "Computer Vision",
                "Large Language Models", "LLMs", "RAG", "OpenAI", "Hugging Face")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.AI_ML));

        // Testing & Tools
        List.of("Git", "GitHub", "GitLab", "Bitbucket", "Postman", "JUnit", "Mockito", "Jest",
                "Cypress", "Selenium", "Jira", "Agile", "Scrum", "Maven", "Gradle")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.TOOLS));

        // Core CS
        List.of("Data Structures", "Algorithms", "Object-Oriented Programming", "OOP",
                "System Design", "Operating Systems", "Computer Networks", "DBMS")
                .forEach(s -> KNOWN_SKILLS.put(s, CareerSkillCategory.CORE_CS));
    }

    public ResumeExtractionService(PdfExtractionService pdfExtractionService,
                                   DocxExtractionService docxExtractionService,
                                   ObjectMapper objectMapper) {
        this.pdfExtractionService = pdfExtractionService;
        this.docxExtractionService = docxExtractionService;
        this.objectMapper = objectMapper;
    }

    public ExtractionResult extractRawText(File file, ResumeFileType fileType) {
        if (fileType == ResumeFileType.PDF) {
            return pdfExtractionService.extract(file);
        } else if (fileType == ResumeFileType.DOCX) {
            return docxExtractionService.extract(file);
        }
        throw new IllegalArgumentException("Unsupported file type: " + fileType);
    }

    public ExtractedResumeDataDto parseStructuredData(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ExtractedResumeDataDto(null, null, null, null, null, null, null,
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }

        String email = extractFirstMatch(rawText, EMAIL_PATTERN);
        String phone = extractFirstMatch(rawText, PHONE_PATTERN);
        String linkedin = extractFirstMatch(rawText, LINKEDIN_PATTERN);
        String github = extractFirstMatch(rawText, GITHUB_PATTERN);
        String portfolio = extractPortfolio(rawText, linkedin, github);
        String fullName = extractFullName(rawText, email);

        Map<String, String> sections = splitSections(rawText);

        String summary = sections.getOrDefault("SUMMARY", null);
        List<String> skills = extractSkills(rawText);
        List<EducationItemDto> education = extractEducation(sections.getOrDefault("EDUCATION", ""));
        List<ExperienceItemDto> experience = extractExperience(sections.getOrDefault("EXPERIENCE", ""));
        List<ProjectItemDto> projects = extractProjects(sections.getOrDefault("PROJECTS", ""));
        List<CertificationItemDto> certifications = extractCertifications(sections.getOrDefault("CERTIFICATIONS", ""));
        List<String> achievements = extractAchievements(sections.getOrDefault("ACHIEVEMENTS", ""));

        return new ExtractedResumeDataDto(
                fullName,
                email,
                phone,
                linkedin,
                github,
                portfolio,
                summary,
                skills,
                education,
                experience,
                projects,
                certifications,
                achievements
        );
    }

    private String extractFirstMatch(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0).trim();
        }
        return null;
    }

    private String extractPortfolio(String text, String linkedin, String github) {
        Matcher matcher = PORTFOLIO_PATTERN.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            if (start > 0 && text.charAt(start - 1) == '@') {
                continue; // Skip email domains
            }
            String url = matcher.group(0).trim();
            String lowerUrl = url.toLowerCase();
            if ((linkedin == null || !lowerUrl.contains("linkedin.com")) &&
                    (github == null || !lowerUrl.contains("github.com"))) {
                return url;
            }
        }
        return null;
    }

    private String extractFullName(String text, String email) {
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(6, lines.length); i++) {
            String line = lines[i].trim();
            if (line.isBlank() || line.length() < 3 || line.length() > 60) continue;
            String lower = line.toLowerCase();
            if (lower.contains("resume") || lower.contains("curriculum") || lower.contains("cv")
                    || lower.contains("@") || lower.contains("http") || lower.contains("phone")
                    || lower.contains("contact") || lower.matches(".*\\d.*")) {
                continue;
            }
            // Looks like a name: 2 to 4 capitalized words
            String[] words = line.split("\\s+");
            if (words.length >= 2 && words.length <= 4) {
                return line;
            }
        }
        return null;
    }

    private Map<String, String> splitSections(String text) {
        Map<String, String> sections = new HashMap<>();
        String[] lines = text.split("\\r?\\n");

        String currentSection = "HEADER";
        StringBuilder currentBuffer = new StringBuilder();

        Pattern headingPattern = Pattern.compile(
                "^(?:#{1,4}\\s+|[•\\-*]\\s+)?(SUMMARY|PROFESSIONAL SUMMARY|EXECUTIVE SUMMARY|PROFILE|ABOUT ME|EDUCATION|ACADEMIC BACKGROUND|ACADEMIC HISTORY|EXPERIENCE|WORK EXPERIENCE|PROFESSIONAL EXPERIENCE|EMPLOYMENT HISTORY|INTERNSHIPS|PROJECTS|TECHNICAL PROJECTS|KEY PROJECTS|ACADEMIC PROJECTS|PERSONAL PROJECTS|TECHNICAL SKILLS|SKILLS & COMPETENCIES|SKILLS|CORE COMPETENCIES|CERTIFICATIONS|CERTIFICATES|LICENSES & CERTIFICATIONS|ACHIEVEMENTS|HONORS & AWARDS|AWARDS|PUBLICATIONS)(?:\\s*[:\\-–—])?\\s*$",
                Pattern.CASE_INSENSITIVE
        );

        for (String rawLine : lines) {
            String line = rawLine.trim();
            Matcher matcher = headingPattern.matcher(line);
            if (matcher.matches()) {
                if (!currentBuffer.isEmpty()) {
                    sections.put(normalizeSectionKey(currentSection), currentBuffer.toString().trim());
                    currentBuffer.setLength(0);
                }
                currentSection = matcher.group(1).toUpperCase();
            } else {
                currentBuffer.append(rawLine).append("\n");
            }
        }

        if (!currentBuffer.isEmpty()) {
            sections.put(normalizeSectionKey(currentSection), currentBuffer.toString().trim());
        }

        return sections;
    }

    private String normalizeSectionKey(String raw) {
        String upper = raw.toUpperCase();
        if (upper.contains("SUMMARY") || upper.contains("ABOUT")) return "SUMMARY";
        if (upper.contains("EDUCATION")) return "EDUCATION";
        if (upper.contains("EXPERIENCE") || upper.contains("INTERNSHIP")) return "EXPERIENCE";
        if (upper.contains("PROJECT")) return "PROJECTS";
        if (upper.contains("SKILL")) return "SKILLS";
        if (upper.contains("CERTIF")) return "CERTIFICATIONS";
        if (upper.contains("ACHIEVE") || upper.contains("AWARD") || upper.contains("HONOR")) return "ACHIEVEMENTS";
        return upper;
    }

    public List<String> extractSkills(String text) {
        if (text == null || text.isBlank()) return List.of();

        Set<String> detected = new LinkedHashSet<>();
        for (String skill : KNOWN_SKILLS.keySet()) {
            // Word boundary regex matching, case-insensitive
            String patternString = "\\b" + Pattern.quote(skill) + "\\b";
            if (skill.equalsIgnoreCase("C++")) {
                patternString = "(?:\\bC\\+\\+|\\bCPP\\b)";
            } else if (skill.equalsIgnoreCase("C#")) {
                patternString = "(?:\\bC#|\\bCSHARP\\b)";
            } else if (skill.equalsIgnoreCase("C")) {
                patternString = "\\bC\\b";
            } else if (skill.equalsIgnoreCase("R")) {
                patternString = "\\bR\\b";
            }

            Pattern p = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            if (p.matcher(text).find()) {
                detected.add(skill);
            }
        }
        return new ArrayList<>(detected);
    }

    private List<EducationItemDto> extractEducation(String educationSection) {
        if (educationSection.isBlank()) return List.of();

        List<EducationItemDto> list = new ArrayList<>();
        String[] blocks = educationSection.split("\\n\\s*\\n");

        for (String block : blocks) {
            if (block.isBlank()) continue;
            String institution = null;
            String degree = null;
            String graduationYear = null;
            String gpa = null;

            String[] lines = block.split("\\r?\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isBlank()) continue;

                // Degree detection
                if (degree == null && (line.matches("(?i).*(B\\.?Tech|B\\.S\\.?|B\\.E\\.?|M\\.?Tech|M\\.S\\.?|Bachelor|Master|Diploma|BCA|MCA).*"))) {
                    degree = line;
                } else if (institution == null && (line.matches("(?i).*(University|College|Institute|School|Academy).*"))) {
                    institution = line;
                }

                // Graduation year detection
                Matcher yearMatcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(line);
                if (yearMatcher.find() && graduationYear == null) {
                    graduationYear = yearMatcher.group(0);
                }

                // GPA / CGPA detection
                Matcher gpaMatcher = Pattern.compile("(?i)(?:GPA|CGPA)[:\\s]*(\\d+(?:\\.\\d+)?(?:/\\d+(?:\\.\\d+)?)?)").matcher(line);
                if (gpaMatcher.find() && gpa == null) {
                    gpa = gpaMatcher.group(1);
                }
            }

            if (institution != null || degree != null) {
                list.add(new EducationItemDto(
                        institution != null ? institution : "Educational Institution",
                        degree != null ? degree : "Degree/Program",
                        null,
                        graduationYear,
                        gpa
                ));
            }
        }

        return list;
    }

    private List<ExperienceItemDto> extractExperience(String experienceSection) {
        if (experienceSection.isBlank()) return List.of();

        List<ExperienceItemDto> list = new ArrayList<>();
        String[] blocks = experienceSection.split("\\n\\s*\\n");

        for (String block : blocks) {
            if (block.isBlank()) continue;
            String company = null;
            String role = null;
            String duration = null;
            List<String> highlights = new ArrayList<>();

            String[] lines = block.split("\\r?\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isBlank()) continue;

                if (role == null && (line.matches("(?i).*(Engineer|Developer|Intern|Consultant|Analyst|Lead|Specialist|Associate).*"))) {
                    role = line;
                } else if (company == null && !line.startsWith("•") && !line.startsWith("-")) {
                    company = line;
                } else if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                    highlights.add(line.replaceFirst("^[•\\-*]\\s*", ""));
                }
            }

            if (company != null || role != null) {
                list.add(new ExperienceItemDto(
                        company != null ? company : "Company / Organization",
                        role != null ? role : "Role",
                        duration,
                        block,
                        highlights
                ));
            }
        }

        return list;
    }

    private List<ProjectItemDto> extractProjects(String projectsSection) {
        if (projectsSection.isBlank()) return List.of();

        List<ProjectItemDto> list = new ArrayList<>();
        String[] blocks = projectsSection.split("\\n\\s*\\n");

        for (String block : blocks) {
            if (block.isBlank()) continue;
            String title = null;
            String techStack = null;
            String liveUrl = null;
            String githubUrl = null;
            List<String> highlights = new ArrayList<>();

            String[] lines = block.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isBlank()) continue;

                if (title == null) {
                    title = line;
                } else if (techStack == null && (line.toLowerCase().contains("tech") || line.toLowerCase().contains("stack") || line.contains("|"))) {
                    techStack = line;
                } else if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*")) {
                    highlights.add(line.replaceFirst("^[•\\-*]\\s*", ""));
                }

                if (line.contains("github.com/")) {
                    githubUrl = extractFirstMatch(line, GITHUB_PATTERN);
                }
            }

            if (techStack == null || techStack.isBlank()) {
                List<String> blockSkills = extractSkills(block);
                if (!blockSkills.isEmpty()) {
                    techStack = String.join(", ", blockSkills);
                }
            }

            Matcher portfolioMatcher = PORTFOLIO_PATTERN.matcher(block);
            while (portfolioMatcher.find()) {
                String found = portfolioMatcher.group(0).trim();
                if (!found.toLowerCase().contains("github.com") && !found.toLowerCase().contains("linkedin.com")) {
                    liveUrl = found;
                    break;
                }
            }

            if (title != null && title.length() > 2) {
                list.add(new ProjectItemDto(
                        title,
                        techStack,
                        block,
                        liveUrl,
                        githubUrl,
                        highlights
                ));
            }
        }

        return list;
    }

    private List<CertificationItemDto> extractCertifications(String certSection) {
        if (certSection.isBlank()) return List.of();

        List<CertificationItemDto> list = new ArrayList<>();
        String[] lines = certSection.split("\\r?\\n");

        for (String raw : lines) {
            String line = raw.trim().replaceFirst("^[•\\-*]\\s*", "");
            if (line.isBlank() || line.length() < 3) continue;

            String name = line;
            String issuer = null;
            String year = null;

            Matcher yearMatcher = Pattern.compile("\\b(19|20)\\d{2}\\b").matcher(line);
            if (yearMatcher.find()) {
                year = yearMatcher.group(0);
            }

            if (line.toLowerCase().contains("aws") || line.toLowerCase().contains("amazon")) {
                issuer = "Amazon Web Services";
            } else if (line.toLowerCase().contains("oracle")) {
                issuer = "Oracle";
            } else if (line.toLowerCase().contains("google")) {
                issuer = "Google Cloud";
            } else if (line.toLowerCase().contains("coursera")) {
                issuer = "Coursera";
            } else if (line.toLowerCase().contains("microsoft")) {
                issuer = "Microsoft";
            }

            list.add(new CertificationItemDto(name, issuer, year));
        }

        return list;
    }

    private List<String> extractAchievements(String achievementsSection) {
        if (achievementsSection.isBlank()) return List.of();

        List<String> list = new ArrayList<>();
        String[] lines = achievementsSection.split("\\r?\\n");

        for (String raw : lines) {
            String line = raw.trim().replaceFirst("^[•\\-*]\\s*", "");
            if (!line.isBlank() && line.length() > 5) {
                list.add(line);
            }
        }
        return list;
    }

    public String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
}

