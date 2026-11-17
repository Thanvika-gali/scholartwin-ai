package com.aischolartwin.service;

import com.aischolartwin.dto.CareerSkillRequest;
import com.aischolartwin.dto.CareerSkillResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.CareerSkillRepository;
import com.aischolartwin.repository.ResumeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CareerSkillService {

    private final CareerSkillRepository careerSkillRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    public CareerSkillService(CareerSkillRepository careerSkillRepository,
                              ResumeRepository resumeRepository,
                              ObjectMapper objectMapper) {
        this.careerSkillRepository = careerSkillRepository;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CareerSkillResponse> getSkills(User user) {
        return careerSkillRepository.findByUserOrderByCategoryAscSkillNameAsc(user).stream()
                .map(CareerSkillResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerSkillResponse> getSkills(User user, String categoryStr) {
        if (categoryStr != null && !categoryStr.isBlank() && !"ALL".equalsIgnoreCase(categoryStr)) {
            try {
                CareerSkillCategory category = CareerSkillCategory.valueOf(categoryStr.toUpperCase().trim());
                return careerSkillRepository.findByUserAndCategory(user, category).stream()
                        .map(CareerSkillResponse::fromEntity)
                        .toList();
            } catch (IllegalArgumentException ignored) {}
        }
        return getSkills(user);
    }

    @Transactional
    public CareerSkillResponse addSkill(User user, CareerSkillRequest request) {
        if (request.skillName() == null || request.skillName().isBlank()) {
            throw new InvalidPayloadException("Skill name is required.");
        }

        String trimmedName = request.skillName().trim();
        Optional<CareerSkill> existing = careerSkillRepository.findByUserAndSkillNameIgnoreCase(user, trimmedName);
        if (existing.isPresent()) {
            throw new InvalidPayloadException("Skill '" + trimmedName + "' is already in your skills inventory.");
        }

        CareerSkillCategory category = request.category() != null
                ? request.category()
                : ResumeExtractionService.KNOWN_SKILLS.getOrDefault(trimmedName, CareerSkillCategory.OTHER);

        CareerSkill skill = new CareerSkill(
                user,
                trimmedName,
                category,
                request.proficiency(),
                request.source() != null ? request.source() : SkillSource.USER_ENTERED,
                request.evidenceText()
        );

        if (request.verified() != null) {
            skill.setVerified(request.verified());
        }

        CareerSkill saved = careerSkillRepository.save(skill);
        return CareerSkillResponse.fromEntity(saved);
    }

    @Transactional
    public CareerSkillResponse updateSkill(User user, Long skillId, CareerSkillRequest request) {
        CareerSkill skill = careerSkillRepository.findByIdAndUser(skillId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));

        if (request.skillName() != null && !request.skillName().isBlank()) {
            String trimmedName = request.skillName().trim();
            if (!skill.getSkillName().equalsIgnoreCase(trimmedName) &&
                    careerSkillRepository.existsByUserAndSkillNameIgnoreCase(user, trimmedName)) {
                throw new InvalidPayloadException("Skill '" + trimmedName + "' already exists in your inventory.");
            }
            skill.setSkillName(trimmedName);
        }

        if (request.category() != null) {
            skill.setCategory(request.category());
        }
        if (request.proficiency() != null) {
            skill.setProficiency(request.proficiency());
        }
        if (request.source() != null) {
            skill.setSource(request.source());
        }
        if (request.evidenceText() != null) {
            skill.setEvidenceText(request.evidenceText().trim());
        }
        if (request.verified() != null) {
            skill.setVerified(request.verified());
        }

        CareerSkill updated = careerSkillRepository.save(skill);
        return CareerSkillResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSkill(User user, Long skillId) {
        CareerSkill skill = careerSkillRepository.findByIdAndUser(skillId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + skillId));
        careerSkillRepository.delete(skill);
    }

    @Transactional
    public List<CareerSkillResponse> importSkillsFromResume(User user, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (resume.getSkillsJson() == null || resume.getSkillsJson().isBlank()) {
            return List.of();
        }

        List<String> resumeSkills = new ArrayList<>();
        try {
            resumeSkills = objectMapper.readValue(resume.getSkillsJson(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }

        List<CareerSkillResponse> imported = new ArrayList<>();
        for (String skillName : resumeSkills) {
            if (skillName == null || skillName.isBlank()) continue;
            String trimmed = skillName.trim();

            CareerSkillCategory category = ResumeExtractionService.KNOWN_SKILLS.getOrDefault(trimmed, CareerSkillCategory.OTHER);
            Optional<CareerSkill> existing = careerSkillRepository.findByUserAndSkillNameIgnoreCase(user, trimmed);

            if (existing.isPresent()) {
                CareerSkill skill = existing.get();
                skill.setVerified(true);
                if (skill.getSource() == SkillSource.USER_ENTERED) {
                    skill.setSource(SkillSource.RESUME);
                }
                skill.setEvidenceText("Verified in uploaded resume: " + resume.getTitle());
                imported.add(CareerSkillResponse.fromEntity(careerSkillRepository.save(skill)));
            } else {
                CareerSkill newSkill = new CareerSkill(
                        user,
                        trimmed,
                        category,
                        SkillProficiency.INTERMEDIATE,
                        SkillSource.RESUME,
                        "Verified in uploaded resume: " + resume.getTitle()
                );
                newSkill.setVerified(true);
                imported.add(CareerSkillResponse.fromEntity(careerSkillRepository.save(newSkill)));
            }
        }

        return imported;
    }
}

