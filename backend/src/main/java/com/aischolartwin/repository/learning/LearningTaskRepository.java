package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningTask;
import com.aischolartwin.entity.learning.RoadmapPhase;
import com.aischolartwin.entity.learning.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningTaskRepository extends JpaRepository<LearningTask, Long> {
    List<LearningTask> findByPhaseOrderByTaskOrderAsc(RoadmapPhase phase);
    List<LearningTask> findByUser(User user);
    List<LearningTask> findByUserAndStatus(User user, TaskStatus status);
    Optional<LearningTask> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, TaskStatus status);
}
