package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.AssessmentAttempt;
import com.aischolartwin.entity.learning.LearningAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    List<AssessmentAttempt> findByUserOrderByCompletedAtDesc(User user);
    List<AssessmentAttempt> findByAssessmentOrderByCompletedAtDesc(LearningAssessment assessment);
    List<AssessmentAttempt> findByUserAndAssessmentOrderByCompletedAtDesc(User user, LearningAssessment assessment);
    Optional<AssessmentAttempt> findByIdAndUser(Long id, User user);

    @Query("SELECT AVG(a.scorePercentage) FROM AssessmentAttempt a WHERE a.user = :user")
    Double calculateAverageScoreByUser(@Param("user") User user);

    long countByUser(User user);
    long countByUserAndPassed(User user, Boolean passed);
}
