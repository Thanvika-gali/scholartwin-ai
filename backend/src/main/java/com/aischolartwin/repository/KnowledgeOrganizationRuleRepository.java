package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeOrganizationRule;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeOrganizationRuleRepository extends JpaRepository<KnowledgeOrganizationRule, Long> {

    List<KnowledgeOrganizationRule> findByUserOrderByPriorityDescCreatedAtAsc(User user);

    List<KnowledgeOrganizationRule> findByUserAndIsEnabledTrueOrderByPriorityDesc(User user);

    Optional<KnowledgeOrganizationRule> findByUserAndId(User user, Long id);

    long countByUser(User user);
}

