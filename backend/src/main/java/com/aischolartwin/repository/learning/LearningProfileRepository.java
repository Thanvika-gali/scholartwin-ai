package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.learning.LearningProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningProfileRepository extends JpaRepository<LearningProfile, Long> {
    Optional<LearningProfile> findByUser(User user);
    Optional<LearningProfile> findByUserId(Long userId);
    boolean existsByUser(User user);
}
