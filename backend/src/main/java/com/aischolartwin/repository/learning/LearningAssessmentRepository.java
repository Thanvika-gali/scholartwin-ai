package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.AssessmentType;
import com.aischolartwin.entity.learning.LearningAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningAssessmentRepository extends JpaRepository<LearningAssessment, Long> {
    List<LearningAssessment> findByUserOrderByCreatedAtDesc(User user);
    List<LearningAssessment> findByUserAndTopicIgnoreCase(User user, String topic);
    List<LearningAssessment> findByUserAndAssessmentType(User user, AssessmentType assessmentType);
    Optional<LearningAssessment> findByIdAndUser(Long id, User user);

    @Query("SELECT a FROM LearningAssessment a LEFT JOIN FETCH a.questions WHERE a.id = :id AND a.user = :user")
    Optional<LearningAssessment> findDetailedByIdAndUser(@Param("id") Long id, @Param("user") User user);

    long countByUser(User user);
}
