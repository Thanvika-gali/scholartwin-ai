package com.aischolartwin.repository;

import com.aischolartwin.entity.JobDescriptionAnalysis;
import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDescriptionAnalysisRepository extends JpaRepository<JobDescriptionAnalysis, Long> {

    Optional<JobDescriptionAnalysis> findByIdAndUser(Long id, User user);

    List<JobDescriptionAnalysis> findByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);

    void deleteByResume(Resume resume);
}

