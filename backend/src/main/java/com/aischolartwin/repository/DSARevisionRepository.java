package com.aischolartwin.repository;

import com.aischolartwin.entity.DSAProblem;
import com.aischolartwin.entity.DSARevision;
import com.aischolartwin.entity.DSARevisionStatus;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DSARevisionRepository extends JpaRepository<DSARevision, Long> {

    Optional<DSARevision> findByIdAndUser(Long id, User user);

    List<DSARevision> findByProblemOrderByScheduledDateDesc(DSAProblem problem);

    List<DSARevision> findByUserOrderByScheduledDateAsc(User user);

    List<DSARevision> findByUserAndStatusOrderByScheduledDateAsc(User user, DSARevisionStatus status);

    List<DSARevision> findByUserAndScheduledDateAndStatus(User user, LocalDate scheduledDate, DSARevisionStatus status);

    List<DSARevision> findByUserAndScheduledDateBeforeAndStatus(User user, LocalDate date, DSARevisionStatus status);

    long countByUserAndStatus(User user, DSARevisionStatus status);

    boolean existsByProblemAndStatus(DSAProblem problem, DSARevisionStatus status);

    void deleteByProblem(DSAProblem problem);
}

