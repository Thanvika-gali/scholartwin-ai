package com.aischolartwin.service;

import com.aischolartwin.dto.AuthResponse;
import com.aischolartwin.dto.LoginRequest;
import com.aischolartwin.dto.RegisterRequest;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.AuthenticationFailedException;
import com.aischolartwin.exception.DisabledAccountException;
import com.aischolartwin.exception.DuplicateEmailException;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Successful registration hashes password, creates user with USER role, and issues token")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Linus Torvalds", "linus@kernel.org", "GitKernelLinux123!");

        when(userRepository.existsByEmailIgnoreCase("linus@kernel.org")).thenReturn(false);
        when(passwordEncoder.encode("GitKernelLinux123!")).thenReturn("$2a$12$encoded_secret_hash");

        User savedUser = new User("Linus Torvalds", "linus@kernel.org", "$2a$12$encoded_secret_hash", Role.USER);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("sample.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("sample.jwt.token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("Linus Torvalds", response.user().fullName());
        assertEquals("linus@kernel.org", response.user().email());
        assertEquals(Role.USER, response.user().role());

        verify(userRepository).save(argThat(user ->
            user.getRole() == Role.USER &&
            user.getEmail().equals("linus@kernel.org") &&
            user.getPasswordHash().equals("$2a$12$encoded_secret_hash")
        ));
    }

    @Test
    @DisplayName("Registration rejects duplicate email with DuplicateEmailException")
    void shouldRejectDuplicateEmailOnRegister() {
        RegisterRequest request = new RegisterRequest("Ken Thompson", "ken@unix.bell", "BUnixLanguage1!");
        when(userRepository.existsByEmailIgnoreCase("ken@unix.bell")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successful login matches password and returns valid token")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("dennis@bell-labs.com", "CLanguage1972!");
        User user = new User("Dennis Ritchie", "dennis@bell-labs.com", "$2a$12$hashed_c_secret", Role.USER);
        user.setId(20L);

        when(userRepository.findByEmailIgnoreCase("dennis@bell-labs.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CLanguage1972!", "$2a$12$hashed_c_secret")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("valid.login.jwt");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid.login.jwt", response.token());
        assertEquals("Dennis Ritchie", response.user().fullName());
    }

    @Test
    @DisplayName("Login with invalid password throws generic AuthenticationFailedException")
    void shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest("dennis@bell-labs.com", "WrongPassword!");
        User user = new User("Dennis Ritchie", "dennis@bell-labs.com", "$2a$12$hashed_c_secret", Role.USER);

        when(userRepository.findByEmailIgnoreCase("dennis@bell-labs.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword!", "$2a$12$hashed_c_secret")).thenReturn(false);

        assertThrows(AuthenticationFailedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login with non-existent email throws generic AuthenticationFailedException")
    void shouldRejectNonExistentEmail() {
        LoginRequest request = new LoginRequest("ghost@nowhere.com", "SomePassword!");
        when(userRepository.findByEmailIgnoreCase("ghost@nowhere.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login rejects disabled account with DisabledAccountException")
    void shouldRejectDisabledAccount() {
        LoginRequest request = new LoginRequest("inactive@school.edu", "ActivePassword123!");
        User user = new User("Inactive User", "inactive@school.edu", "$2a$12$hash", Role.USER);
        user.setActive(false);

        when(userRepository.findByEmailIgnoreCase("inactive@school.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ActivePassword123!", "$2a$12$hash")).thenReturn(true);

        assertThrows(DisabledAccountException.class, () -> authService.login(request));
    }
}

