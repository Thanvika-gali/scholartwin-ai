package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeCollection;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeCollectionRepository extends JpaRepository<KnowledgeCollection, Long> {

    List<KnowledgeCollection> findByUserOrderByNameAsc(User user);

    Optional<KnowledgeCollection> findByUserAndId(User user, Long id);

    long countByUser(User user);
}

