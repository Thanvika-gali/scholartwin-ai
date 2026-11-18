package com.aischolartwin.dto.intelligence;

import com.aischolartwin.entity.intelligence.MemoryPreference;

public class MemoryPreferenceDto {

    public record Request(
            Boolean memoryEnabled,
            Boolean learningMemoryEnabled,
            Boolean technicalMemoryEnabled,
            Boolean careerMemoryEnabled,
            Boolean knowledgeMemoryEnabled,
            Boolean interactionMemoryEnabled,
            Boolean autoExtractFromActivity,
            Boolean autoExtractFromChat
    ) {}

    public record Response(
            Long id,
            Boolean memoryEnabled,
            Boolean learningMemoryEnabled,
            Boolean technicalMemoryEnabled,
            Boolean careerMemoryEnabled,
            Boolean knowledgeMemoryEnabled,
            Boolean interactionMemoryEnabled,
            Boolean autoExtractFromActivity,
            Boolean autoExtractFromChat
    ) {
        public static Response fromEntity(MemoryPreference p) {
            return new Response(
                    p.getId(),
                    p.getMemoryEnabled(),
                    p.getLearningMemoryEnabled(),
                    p.getTechnicalMemoryEnabled(),
                    p.getCareerMemoryEnabled(),
                    p.getKnowledgeMemoryEnabled(),
                    p.getInteractionMemoryEnabled(),
                    p.getAutoExtractFromActivity(),
                    p.getAutoExtractFromChat()
            );
        }
    }
}
