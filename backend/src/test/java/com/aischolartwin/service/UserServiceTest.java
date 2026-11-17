package com.aischolartwin.service;

import com.aischolartwin.dto.UpdateProfileRequest;
import com.aischolartwin.dto.UserResponse;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.UserNotFoundException;
import com.aischolartwin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Should return user profile successfully for existing email")
    void shouldGetProfileSuccessfully() {
        User user = new User("Donald Knuth", "knuth@stanford.edu", "hash", Role.USER);
        user.setId(55L);
        when(userRepository.findByEmailIgnoreCase("knuth@stanford.edu")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("knuth@stanford.edu");

        assertNotNull(response);
        assertEquals(55L, response.id());
        assertEquals("Donald Knuth", response.fullName());
        assertEquals("knuth@stanford.edu", response.email());
    }

    @Test
    @DisplayName("Should update user full name and return updated profile")
    void shouldUpdateProfileSuccessfully() {
        User user = new User("Old Name", "scholar@mit.edu", "hash", Role.USER);
        user.setId(56L);
        when(userRepository.findByEmailIgnoreCase("scholar@mit.edu")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest("New Scholar Name");
        UserResponse response = userService.updateProfile("scholar@mit.edu", request);

        assertNotNull(response);
        assertEquals("New Scholar Name", response.fullName());
        verify(userRepository).save(argThat(u -> u.getFullName().equals("New Scholar Name")));
    }

    @Test
    @DisplayName("Update profile throws UserNotFoundException if user does not exist")
    void shouldThrowWhenUserNotFoundOnUpdate() {
        when(userRepository.findByEmailIgnoreCase("unknown@nowhere.org")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
            userService.updateProfile("unknown@nowhere.org", new UpdateProfileRequest("Any Name"))
        );
    }
}

