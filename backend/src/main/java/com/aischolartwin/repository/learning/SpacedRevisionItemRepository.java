package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.RevisionItemType;
import com.aischolartwin.entity.learning.RevisionStatus;
import com.aischolartwin.entity.learning.SpacedRevisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpacedRevisionItemRepository extends JpaRepository<SpacedRevisionItem, Long> {
    List<SpacedRevisionItem> findByUserOrderByScheduledDateAsc(User user);
    List<SpacedRevisionItem> findByUserAndStatusOrderByScheduledDateAsc(User user, RevisionStatus status);
    List<SpacedRevisionItem> findByUserAndScheduledDateLessThanEqualAndStatusNot(User user, LocalDate date, RevisionStatus status);
    List<SpacedRevisionItem> findByUserAndItemTypeAndLinkedReferenceId(User user, RevisionItemType itemType, Long linkedReferenceId);
    Optional<SpacedRevisionItem> findByIdAndUser(Long id, User user);
    long countByUserAndStatus(User user, RevisionStatus status);
    long countByUser(User user);
}
