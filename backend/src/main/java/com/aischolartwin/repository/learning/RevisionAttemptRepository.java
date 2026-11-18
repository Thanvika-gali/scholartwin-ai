package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.RevisionAttempt;
import com.aischolartwin.entity.learning.SpacedRevisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RevisionAttemptRepository extends JpaRepository<RevisionAttempt, Long> {
    List<RevisionAttempt> findByRevisionItemOrderByAttemptedAtDesc(SpacedRevisionItem revisionItem);
    List<RevisionAttempt> findByUserOrderByAttemptedAtDesc(User user);
    long countByUser(User user);
}
