package com.aischolartwin.repository;

import com.aischolartwin.entity.DSAAttempt;
import com.aischolartwin.entity.DSAAttemptOutcome;
import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DSAAttemptRepository extends JpaRepository<DSAAttempt, Long> {

    List<DSAAttempt> findByProblemOrderByAttemptDateDescCreatedAtDesc(DSAProblem problem);

    List<DSAAttempt> findByUserOrderByAttemptDateDescCreatedAtDesc(User user);

    List<DSAAttempt> findByUserAndAttemptDateBetweenOrderByAttemptDateAsc(
            User user, LocalDate startDate, LocalDate endDate);

    long countByUser(User user);

    long countByUserAndOutcome(User user, DSAAttemptOutcome outcome);

    long countByProblem(DSAProblem problem);

    long countByProblemAndOutcome(DSAProblem problem, DSAAttemptOutcome outcome);

    List<DSAAttempt> findTop5ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT DISTINCT a.attemptDate FROM DSAAttempt a WHERE a.user = :user ORDER BY a.attemptDate DESC")
    List<LocalDate> findDistinctAttemptDatesByUser(@Param("user") User user);

    @Query("SELECT AVG(a.timeSpentMinutes) FROM DSAAttempt a WHERE a.problem = :problem AND a.timeSpentMinutes IS NOT NULL")
    Double findAverageTimeSpentByProblem(@Param("problem") DSAProblem problem);

    void deleteByProblem(DSAProblem problem);
}

