package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserOrderBySessionDateDescCreatedAtDesc(User user);
    List<StudySession> findByUserAndSessionDateBetweenOrderBySessionDateAsc(User user, LocalDate startDate, LocalDate endDate);
    Optional<StudySession> findByIdAndUser(Long id, User user);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s WHERE s.user = :user")
    Long sumTotalMinutesByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s WHERE s.user = :user AND s.sessionDate BETWEEN :startDate AND :endDate")
    Long sumMinutesByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT s.sessionDate FROM StudySession s WHERE s.user = :user ORDER BY s.sessionDate DESC")
    List<LocalDate> findDistinctSessionDatesByUser(@Param("user") User user);
}
