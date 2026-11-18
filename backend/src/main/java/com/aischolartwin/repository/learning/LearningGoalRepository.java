package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningGoal;
import com.aischolartwin.entity.learning.PlannerTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {
    List<LearningGoal> findByUserOrderByCreatedAtDesc(User user);
    List<LearningGoal> findByUserAndStatusOrderByTargetDateAsc(User user, PlannerTaskStatus status);
    Optional<LearningGoal> findByIdAndUser(Long id, User user);
    long countByUserAndStatus(User user, PlannerTaskStatus status);
}
