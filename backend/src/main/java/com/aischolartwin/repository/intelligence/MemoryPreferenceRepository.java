package com.aischolartwin.repository.intelligence;

import com.aischolartwin.entity.User;
import com.aischolartwin.entity.intelligence.MemoryPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemoryPreferenceRepository extends JpaRepository<MemoryPreference, Long> {

    Optional<MemoryPreference> findByUser(User user);

    boolean existsByUser(User user);

    void deleteByUser(User user);
}
