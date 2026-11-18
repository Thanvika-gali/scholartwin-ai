package com.aischolartwin.service.intelligence;

import com.aischolartwin.dto.intelligence.MemoryPreferenceDto;
import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.MemoryPreference;
import com.aischolartwin.repository.intelligence.MemoryPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemoryPreferenceService {

    private final MemoryPreferenceRepository preferenceRepository;

    public MemoryPreferenceService(MemoryPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional
    public MemoryPreferenceDto.Response getOrCreatePreferences(User user) {
        MemoryPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> preferenceRepository.save(new MemoryPreference(user)));
        return MemoryPreferenceDto.Response.fromEntity(pref);
    }

    @Transactional
    public MemoryPreferenceDto.Response updatePreferences(User user, MemoryPreferenceDto.Request request) {
        MemoryPreference pref = preferenceRepository.findByUser(user)
                .orElseGet(() -> new MemoryPreference(user));

        if (request.memoryEnabled() != null) {
            pref.setMemoryEnabled(request.memoryEnabled());
        }
        if (request.learningMemoryEnabled() != null) {
            pref.setLearningMemoryEnabled(request.learningMemoryEnabled());
        }
        if (request.technicalMemoryEnabled() != null) {
            pref.setTechnicalMemoryEnabled(request.technicalMemoryEnabled());
        }
        if (request.careerMemoryEnabled() != null) {
            pref.setCareerMemoryEnabled(request.careerMemoryEnabled());
        }
        if (request.knowledgeMemoryEnabled() != null) {
            pref.setKnowledgeMemoryEnabled(request.knowledgeMemoryEnabled());
        }
        if (request.interactionMemoryEnabled() != null) {
            pref.setInteractionMemoryEnabled(request.interactionMemoryEnabled());
        }
        if (request.autoExtractFromActivity() != null) {
            pref.setAutoExtractFromActivity(request.autoExtractFromActivity());
        }
        if (request.autoExtractFromChat() != null) {
            pref.setAutoExtractFromChat(request.autoExtractFromChat());
        }

        MemoryPreference saved = preferenceRepository.save(pref);
        return MemoryPreferenceDto.Response.fromEntity(saved);
    }
}
