package com.aischolartwin.repository;

import com.aischolartwin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for User persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email, matching case-insensitively.
     *
     * @param email User email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Finds a user by exact email.
     *
     * @param email User email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email, case-insensitively.
     *
     * @param email User email address to check
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if a user already exists with the given exact email.
     *
     * @param email User email address to check
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
}

