package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.AIRecommendation;
import com.aischolartwin.entity.intelligence.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, Long> {

    List<AIRecommendation> findByUserAndStatusOrderByCreatedAtDesc(User user, RecommendationStatus status);

    List<AIRecommendation> findByUserOrderByCreatedAtDesc(User user);

    Optional<AIRecommendation> findByIdAndUser(Long id, User user);

    long countByUserAndStatus(User user, RecommendationStatus status);

    boolean existsByUserAndTitleIgnoreCaseAndStatus(User user, String title, RecommendationStatus status);

    boolean existsByUserAndTitleIgnoreCaseAndStatusIn(User user, String title, Collection<RecommendationStatus> statuses);

    void deleteByUser(User user);
}
