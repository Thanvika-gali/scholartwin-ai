package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeItem;
import com.aischolartwin.entity.KnowledgeItemType;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for KnowledgeItem persistence operations.
 * Enforces ownership boundary by scoping lookups to the authenticated User.
 */
@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, Long> {

    List<KnowledgeItem> findByUserOrderByUpdatedAtDesc(User user);

    List<KnowledgeItem> findByUserAndTypeOrderByUpdatedAtDesc(User user, KnowledgeItemType type);

    List<KnowledgeItem> findTop5ByUserOrderByUpdatedAtDesc(User user);

    long countByUser(User user);

    long countByUserAndType(User user, KnowledgeItemType type);

    Optional<KnowledgeItem> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    List<KnowledgeItem> findByUserAndFolderIdOrderByUpdatedAtDesc(User user, Long folderId);

    List<KnowledgeItem> findByUserAndFolderIdIsNullAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<KnowledgeItem> findByUserAndIsFavoriteTrueAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<KnowledgeItem> findByUserAndIsPinnedTrueAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<KnowledgeItem> findByUserAndIsArchivedTrueOrderByUpdatedAtDesc(User user);

    List<KnowledgeItem> findByUserAndUrl(User user, String url);

    long countByUserAndFolderId(User user, Long folderId);

    long countByUserAndFolderIdIsNullAndIsArchivedFalse(User user);

    long countByUserAndIsFavoriteTrue(User user);

    long countByUserAndIsArchivedTrue(User user);

    @Query("SELECT k FROM KnowledgeItem k WHERE k.user = :user AND " +
           "(:type IS NULL OR k.type = :type) AND " +
           "(:category IS NULL OR LOWER(k.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(k.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(k.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(k.url, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY k.updatedAt DESC")
    List<KnowledgeItem> searchItems(
        @Param("user") User user,
        @Param("type") KnowledgeItemType type,
        @Param("category") String category,
        @Param("keyword") String keyword
    );
}
