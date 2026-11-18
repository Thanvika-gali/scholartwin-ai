package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.intelligence.Insight;
import com.aischolartwin.entity.intelligence.InsightEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsightEvidenceRepository extends JpaRepository<InsightEvidence, Long> {

    List<InsightEvidence> findByInsight(Insight insight);
}
