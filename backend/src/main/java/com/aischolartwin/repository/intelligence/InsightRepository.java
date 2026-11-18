package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.Insight;
import com.aischolartwin.entity.intelligence.InsightCategory;
import com.aischolartwin.entity.intelligence.InsightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findByUserAndStatusOrderByGeneratedAtDesc(User user, InsightStatus status);

    List<Insight> findByUserAndCategoryAndStatusOrderByGeneratedAtDesc(User user, InsightCategory category, InsightStatus status);

    Optional<Insight> findByIdAndUser(Long id, User user);

    long countByUserAndStatus(User user, InsightStatus status);

    void deleteByUser(User user);
}
