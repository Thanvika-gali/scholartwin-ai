package com.aischolartwin.repository;

import com.aischolartwin.entity.DSADifficulty;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSAStatus;
import com.aischolartwin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DSAProblemRepository extends JpaRepository<DSAProblem, Long> {

    Optional<DSAProblem> findByIdAndUser(Long id, User user);

    List<DSAProblem> findByUserOrderByUpdatedAtDesc(User user);

    long countByUser(User user);

    long countByUserAndStatus(User user, DSAStatus status);

    long countByUserAndDifficulty(User user, DSADifficulty difficulty);

    long countByUserAndDifficultyAndStatus(User user, DSADifficulty difficulty, DSAStatus status);

    long countByUserAndPrimaryTopic(User user, String primaryTopic);

    long countByUserAndPrimaryTopicAndStatus(User user, String primaryTopic, DSAStatus status);

    List<DSAProblem> findByUserAndRevisionRequiredTrue(User user);

    List<DSAProblem> findTop5ByUserOrderByUpdatedAtDesc(User user);

    List<DSAProblem> findTop5ByUserAndStatusOrderBySolvedAtDesc(User user, DSAStatus status);

    List<DSAProblem> findTop5ByUserAndStatusOrderByUpdatedAtDesc(User user, DSAStatus status);

    @Query("""
        SELECT p FROM DSAProblem p
        WHERE p.user = :user
          AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))
                               OR LOWER(p.personalNotes) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:topic IS NULL OR p.primaryTopic = :topic OR :topic MEMBER OF p.additionalTopics)
          AND (:difficulty IS NULL OR p.difficulty = :difficulty)
          AND (:platform IS NULL OR LOWER(p.platform) = LOWER(:platform))
          AND (:status IS NULL OR p.status = :status)
          AND (:revisionRequired IS NULL OR p.revisionRequired = :revisionRequired)
    """)
    Page<DSAProblem> searchProblems(
            @Param("user") User user,
            @Param("search") String search,
            @Param("topic") String topic,
            @Param("difficulty") DSADifficulty difficulty,
            @Param("platform") String platform,
            @Param("status") DSAStatus status,
            @Param("revisionRequired") Boolean revisionRequired,
            Pageable pageable
    );

    List<DSAProblem> findByUserAndPrimaryTopic(User user, String primaryTopic);
}

