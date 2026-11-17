package com.aischolartwin.repository;

import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.ResumeAnalysis;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByIdAndUser(Long id, User user);

    List<ResumeAnalysis> findByUserOrderByCreatedAtDesc(User user);

    List<ResumeAnalysis> findByResumeOrderByCreatedAtDesc(Resume resume);

    Optional<ResumeAnalysis> findTopByResumeOrderByCreatedAtDesc(Resume resume);

    Optional<ResumeAnalysis> findTopByUserOrderByCreatedAtDesc(User user);

    void deleteByResume(Resume resume);
}

