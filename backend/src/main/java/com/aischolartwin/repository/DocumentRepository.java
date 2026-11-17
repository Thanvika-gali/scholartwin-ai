package com.aischolartwin.repository;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentFormat;
import com.aischolartwin.entity.DocumentStatus;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserOrderByUpdatedAtDesc(User user);

    Optional<Document> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndStatus(User user, DocumentStatus status);

    List<Document> findByUserAndFileHash(User user, String fileHash);

    List<Document> findByUserAndFolderIdOrderByUpdatedAtDesc(User user, Long folderId);

    List<Document> findByUserAndFolderIdIsNullAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<Document> findByUserAndIsFavoriteTrueAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<Document> findByUserAndIsPinnedTrueAndIsArchivedFalseOrderByUpdatedAtDesc(User user);

    List<Document> findByUserAndIsArchivedTrueOrderByUpdatedAtDesc(User user);

    List<Document> findByUserAndParentDocumentIdOrderByVersionNumberDesc(User user, Long parentDocumentId);

    long countByUserAndFolderId(User user, Long folderId);

    long countByUserAndFolderIdIsNullAndIsArchivedFalse(User user);

    long countByUserAndIsFavoriteTrue(User user);

    long countByUserAndIsArchivedTrue(User user);

    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.user = :user")
    long sumFileSizeByUser(@Param("user") User user);

    @Query("SELECT d FROM Document d WHERE d.user = :user " +
            "AND (:status IS NULL OR d.status = :status) " +
            "AND (:format IS NULL OR d.format = :format) " +
            "AND (:search IS NULL OR LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY d.updatedAt DESC")
    List<Document> searchDocuments(
            @Param("user") User user,
            @Param("status") DocumentStatus status,
            @Param("format") DocumentFormat format,
            @Param("search") String search
    );

    @Query("SELECT d FROM Document d WHERE d.user = :user AND d.isArchived = false " +
            "AND (:folderId IS NULL OR d.folderId = :folderId) " +
            "AND (:search IS NULL OR LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(COALESCE(d.documentSummary, '')) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY d.updatedAt DESC")
    List<Document> findActiveByUserAndFolderAndSearch(
            @Param("user") User user,
            @Param("folderId") Long folderId,
            @Param("search") String search
    );
}
