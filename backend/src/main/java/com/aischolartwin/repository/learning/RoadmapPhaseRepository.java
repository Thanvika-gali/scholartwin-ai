package com.aischolartwin.repository.learning;

import com.aischolartwin.entity.learning.LearningRoadmap;
import com.aischolartwin.entity.learning.RoadmapPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapPhaseRepository extends JpaRepository<RoadmapPhase, Long> {
    List<RoadmapPhase> findByRoadmapOrderByPhaseOrderAsc(LearningRoadmap roadmap);
    Optional<RoadmapPhase> findByIdAndRoadmap(Long id, LearningRoadmap roadmap);
}
