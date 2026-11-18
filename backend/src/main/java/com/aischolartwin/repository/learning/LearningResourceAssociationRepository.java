package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningResourceAssociation;
import com.aischolartwin.entity.learning.LearningResourceType;
import com.aischolartwin.entity.learning.LearningTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningResourceAssociationRepository extends JpaRepository<LearningResourceAssociation, Long> {
    List<LearningResourceAssociation> findByUserOrderByCreatedAtDesc(User user);
    List<LearningResourceAssociation> findByUserAndTask(User user, LearningTask task);
    List<LearningResourceAssociation> findByUserAndTopicIgnoreCase(User user, String topic);
    List<LearningResourceAssociation> findByUserAndResourceType(User user, LearningResourceType resourceType);
    Optional<LearningResourceAssociation> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    long countByUserAndStudied(User user, Boolean studied);
}
