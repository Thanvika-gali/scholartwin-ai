package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeOrganizationSuggestion;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.SuggestionStatus;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeOrganizationSuggestionRepository extends JpaRepository<KnowledgeOrganizationSuggestion, Long> {

    List<KnowledgeOrganizationSuggestion> findByUserAndStatusOrderByCreatedAtDesc(User user, SuggestionStatus status);

    List<KnowledgeOrganizationSuggestion> findByUserOrderByCreatedAtDesc(User user);

    Optional<KnowledgeOrganizationSuggestion> findByUserAndId(User user, Long id);

    Optional<KnowledgeOrganizationSuggestion> findByUserAndResourceTypeAndResourceId(
            User user, ResourceType resourceType, Long resourceId);

    long countByUserAndStatus(User user, SuggestionStatus status);

    void deleteByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);
}

