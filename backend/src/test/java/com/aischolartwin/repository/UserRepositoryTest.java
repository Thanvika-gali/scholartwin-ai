package com.aischolartwin.repository;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist user and find by email case-insensitively")
    void shouldSaveAndFindByEmailIgnoreCase() {
        User user = new User("Hypatia of Alexandria", "hypatia@library.alexandria.eg", "secure_hash_123", Role.USER);
        User saved = userRepository.saveAndFlush(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<User> found = userRepository.findByEmailIgnoreCase("HYPATIA@LIBRARY.ALEXANDRIA.EG");
        assertTrue(found.isPresent());
        assertEquals("Hypatia of Alexandria", found.get().getFullName());
        assertEquals("hypatia@library.alexandria.eg", found.get().getEmail());
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase should return true for existing email and false for unknown email")
    void existsByEmailIgnoreCaseShouldDetectPresence() {
        User user = new User("Johannes Kepler", "kepler@astronomy.org", "hash_planetary_motion", Role.USER);
        userRepository.saveAndFlush(user);

        assertTrue(userRepository.existsByEmailIgnoreCase("KEPLER@ASTRONOMY.ORG"));
        assertTrue(userRepository.existsByEmailIgnoreCase("kepler@astronomy.org"));
        assertFalse(userRepository.existsByEmailIgnoreCase("newton@gravity.org"));
    }

    @Test
    @DisplayName("Saving duplicate email should trigger unique constraint violation")
    void savingDuplicateEmailShouldThrowDataIntegrityViolation() {
        User first = new User("Galileo Galilei", "galileo@telescope.it", "hash_one", Role.USER);
        userRepository.saveAndFlush(first);

        User duplicate = new User("Galileo Clone", "galileo@telescope.it", "hash_two", Role.USER);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicate);
        });
    }
}

