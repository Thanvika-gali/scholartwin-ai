package com.aischolartwin.repository;

import com.aischolartwin.entity.Resume;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByIdAndUser(Long id, User user);

    List<Resume> findByUserOrderByCreatedAtDesc(User user);

    Optional<Resume> findByUserAndIsPrimaryTrue(User user);

    long countByUser(User user);

    Optional<Resume> findTopByUserOrderByVersionNumberDesc(User user);

    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = false WHERE r.user = :user AND r.id != :activeResumeId")
    void clearOtherPrimaryResumes(@Param("user") User user, @Param("activeResumeId") Long activeResumeId);
}

