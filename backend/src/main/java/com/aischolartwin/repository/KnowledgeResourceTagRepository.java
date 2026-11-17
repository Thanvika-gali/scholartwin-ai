package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeResourceTag;
import com.aischolartwin.entity.KnowledgeTag;
import com.aischolartwin.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeResourceTagRepository extends JpaRepository<KnowledgeResourceTag, Long> {

    List<KnowledgeResourceTag> findByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);

    Optional<KnowledgeResourceTag> findByTagAndResourceTypeAndResourceId(
            KnowledgeTag tag, ResourceType resourceType, Long resourceId);

    void deleteByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);

    @Query("SELECT krt.tag FROM KnowledgeResourceTag krt WHERE krt.resourceType = :type AND krt.resourceId = :id")
    List<KnowledgeTag> findTagsByResource(@Param("type") ResourceType type, @Param("id") Long id);

    @Query("SELECT krt FROM KnowledgeResourceTag krt WHERE krt.tag IN :tags AND NOT (krt.resourceType = :type AND krt.resourceId = :id)")
    List<KnowledgeResourceTag> findSharedTagResources(
            @Param("tags") List<KnowledgeTag> tags,
            @Param("type") ResourceType type,
            @Param("id") Long id);
}

