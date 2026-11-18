package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.PlannerTask;
import com.aischolartwin.entity.learning.PlannerTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlannerTaskRepository extends JpaRepository<PlannerTask, Long> {
    List<PlannerTask> findByUserAndScheduledDateOrderByPriorityDescCreatedAtAsc(User user, LocalDate scheduledDate);
    List<PlannerTask> findByUserAndScheduledDateBetweenOrderByScheduledDateAscPriorityDesc(User user, LocalDate startDate, LocalDate endDate);
    List<PlannerTask> findByUserAndStatusOrderByScheduledDateAsc(User user, PlannerTaskStatus status);
    Optional<PlannerTask> findByIdAndUser(Long id, User user);
    long countByUserAndScheduledDate(User user, LocalDate scheduledDate);
    long countByUserAndScheduledDateAndStatus(User user, LocalDate scheduledDate, PlannerTaskStatus status);
}
