package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeFolder;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeFolderRepository extends JpaRepository<KnowledgeFolder, Long> {

    List<KnowledgeFolder> findByUserAndParentIsNullOrderByNameAsc(User user);

    List<KnowledgeFolder> findByUserAndParentOrderByNameAsc(User user, KnowledgeFolder parent);

    List<KnowledgeFolder> findByUserOrderByNameAsc(User user);

    Optional<KnowledgeFolder> findByUserAndId(User user, Long id);

    Optional<KnowledgeFolder> findByUserAndNameIgnoreCase(User user, String name);

    long countByUser(User user);

    List<KnowledgeFolder> findByUserAndIsFavoriteTrue(User user);

    List<KnowledgeFolder> findByUserAndIsPinnedTrue(User user);

    @Query("SELECT f FROM KnowledgeFolder f WHERE f.user = :user AND LOWER(f.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<KnowledgeFolder> searchByName(@Param("user") User user, @Param("term") String term);
}

