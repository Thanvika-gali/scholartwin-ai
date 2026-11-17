package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeCollection;
import com.aischolartwin.entity.KnowledgeCollectionItem;
import com.aischolartwin.entity.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeCollectionItemRepository extends JpaRepository<KnowledgeCollectionItem, Long> {

    List<KnowledgeCollectionItem> findByCollectionOrderByAddedAtDesc(KnowledgeCollection collection);

    Optional<KnowledgeCollectionItem> findByCollectionAndResourceTypeAndResourceId(
            KnowledgeCollection collection, ResourceType resourceType, Long resourceId);

    void deleteByCollectionAndResourceTypeAndResourceId(
            KnowledgeCollection collection, ResourceType resourceType, Long resourceId);

    long countByCollection(KnowledgeCollection collection);

    void deleteByResourceTypeAndResourceId(ResourceType resourceType, Long resourceId);
}

