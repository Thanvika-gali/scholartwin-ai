package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningRoadmap;
import com.aischolartwin.entity.learning.RoadmapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningRoadmapRepository extends JpaRepository<LearningRoadmap, Long> {
    List<LearningRoadmap> findByUserOrderByCreatedAtDesc(User user);
    List<LearningRoadmap> findByUserAndStatusOrderByCreatedAtDesc(User user, RoadmapStatus status);
    Optional<LearningRoadmap> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, RoadmapStatus status);

    @Query("SELECT r FROM LearningRoadmap r LEFT JOIN FETCH r.phases WHERE r.id = :id AND r.user = :user")
    Optional<LearningRoadmap> findDetailedByIdAndUser(@Param("id") Long id, @Param("user") User user);
}
