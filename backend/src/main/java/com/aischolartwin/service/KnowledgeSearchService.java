package com.aischolartwin.service;

import com.aischolartwin.dto.KnowledgeSearchResponse;
import com.aischolartwin.dto.ResourceItemResponse;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class KnowledgeSearchService {

    private final KnowledgeResourceService resourceService;

    public KnowledgeSearchService(KnowledgeResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Transactional(readOnly = true)
    public KnowledgeSearchResponse search(
            User user,
            String query,
            ResourceType type,
            Long folderId,
            Boolean favoriteOnly,
            Boolean archivedOnly,
            String sortBy
    ) {
        List<ResourceItemResponse> all = resourceService.listResources(
                user,
                type,
                folderId,
                false,
                favoriteOnly,
                false,
                archivedOnly,
                query,
                sortBy != null ? sortBy : "RECENT_MODIFIED"
        );

        Map<String, Long> facets = new LinkedHashMap<>();
        facets.put("ALL", (long) all.size());
        facets.put("DOCUMENT", all.stream().filter(r -> r.resourceType() == ResourceType.DOCUMENT).count());
        facets.put("NOTE", all.stream().filter(r -> r.resourceType() == ResourceType.NOTE).count());
        facets.put("LINK", all.stream().filter(r -> r.resourceType() == ResourceType.LINK).count());

        return new KnowledgeSearchResponse(
                all,
                all.size(),
                false, // semanticSearchAvailable: false until M5 embeddings configured
                query != null ? query : "",
                facets
        );
    }
}

