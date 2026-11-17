package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeResourceRelation;
import com.aischolartwin.entity.ResourceType;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeResourceRelationRepository extends JpaRepository<KnowledgeResourceRelation, Long> {

    @Query("SELECT r FROM KnowledgeResourceRelation r WHERE r.user = :user AND " +
            "((r.sourceType = :type AND r.sourceId = :id) OR (r.targetType = :type AND r.targetId = :id))")
    List<KnowledgeResourceRelation> findAllRelationsForResource(
            @Param("user") User user,
            @Param("type") ResourceType type,
            @Param("id") Long id);

    Optional<KnowledgeResourceRelation> findByUserAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            User user, ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId);

    void deleteByUserAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            User user, ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId);

    void deleteBySourceTypeAndSourceIdOrTargetTypeAndTargetId(
            ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId);
}

