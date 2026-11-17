package com.aischolartwin.repository;

import com.aischolartwin.entity.CareerSkill;
import com.aischolartwin.entity.CareerSkillCategory;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerSkillRepository extends JpaRepository<CareerSkill, Long> {

    Optional<CareerSkill> findByIdAndUser(Long id, User user);

    Optional<CareerSkill> findByUserAndSkillNameIgnoreCase(User user, String skillName);

    List<CareerSkill> findByUserOrderByCategoryAscSkillNameAsc(User user);

    List<CareerSkill> findByUserAndCategory(User user, CareerSkillCategory category);

    long countByUser(User user);

    long countByUserAndCategory(User user, CareerSkillCategory category);

    boolean existsByUserAndSkillNameIgnoreCase(User user, String skillName);
}

