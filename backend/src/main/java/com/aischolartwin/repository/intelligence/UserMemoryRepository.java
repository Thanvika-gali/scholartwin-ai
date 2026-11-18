package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.MemoryCategory;
import com.aischolartwin.entity.intelligence.MemoryStatus;
import com.aischolartwin.entity.intelligence.UserMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMemoryRepository extends JpaRepository<UserMemory, Long> {

    List<UserMemory> findByUser(User user);

    List<UserMemory> findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc(User user, MemoryStatus status);

    List<UserMemory> findByUserAndCategoryAndStatusOrderByIsPinnedDescUpdatedAtDesc(User user, MemoryCategory category, MemoryStatus status);

    List<UserMemory> findByUserAndIsPinnedTrueAndStatusOrderByUpdatedAtDesc(User user, MemoryStatus status);

    Optional<UserMemory> findByIdAndUser(Long id, User user);

    @Query("SELECT m FROM UserMemory m WHERE m.user = :user AND m.status = :status AND " +
            "(LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(m.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(m.sourceReference) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY m.isPinned DESC, m.updatedAt DESC")
    List<UserMemory> searchMemories(@Param("user") User user,
                                   @Param("status") MemoryStatus status,
                                   @Param("query") String query);

    long countByUserAndStatus(User user, MemoryStatus status);

    List<UserMemory> findByUserAndStatus(User user, MemoryStatus status);

    void deleteByUser(User user);

    boolean existsByUserAndContentIgnoreCaseAndStatusNot(User user, String content, MemoryStatus status);
}
