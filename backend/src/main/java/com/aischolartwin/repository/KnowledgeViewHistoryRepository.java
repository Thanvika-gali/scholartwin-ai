package com.aischolartwin.repository;

import com.aischolartwin.entity.KnowledgeViewHistory;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeViewHistoryRepository extends JpaRepository<KnowledgeViewHistory, Long> {

    List<KnowledgeViewHistory> findTop30ByUserOrderByViewedAtDesc(User user);

    void deleteByUser(User user);
}

