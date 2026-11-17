package com.aischolartwin.repository;

import com.aischolartwin.entity.CareerAction;
import com.aischolartwin.entity.CareerActionStatus;
import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerActionRepository extends JpaRepository<CareerAction, Long> {

    Optional<CareerAction> findByIdAndUser(Long id, User user);

    List<CareerAction> findByUserOrderBySuggestedDeadlineAscCreatedAtDesc(User user);

    List<CareerAction> findByUserAndStatusOrderBySuggestedDeadlineAscCreatedAtDesc(User user, CareerActionStatus status);

    long countByUser(User user);

    long countByUserAndStatus(User user, CareerActionStatus status);

    List<CareerAction> findTop5ByUserOrderByCreatedAtDesc(User user);
}

