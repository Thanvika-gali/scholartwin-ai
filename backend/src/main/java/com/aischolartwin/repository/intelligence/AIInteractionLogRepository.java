package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.AIInteractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInteractionLogRepository extends JpaRepository<AIInteractionLog, Long> {

    List<AIInteractionLog> findByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);

    void deleteByUser(User user);
}
