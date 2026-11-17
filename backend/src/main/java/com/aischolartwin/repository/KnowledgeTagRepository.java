package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeTag;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeTagRepository extends JpaRepository<KnowledgeTag, Long> {

    List<KnowledgeTag> findByUserOrderByNameAsc(User user);

    Optional<KnowledgeTag> findByUserAndNameIgnoreCase(User user, String name);
}

