package com.aischolartwin.service;

import com.aischolartwin.dto.AuthResponse;
import com.aischolartwin.dto.LoginRequest;
import com.aischolartwin.dto.RegisterRequest;
import com.aischolartwin.dto.UserResponse;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.AuthenticationFailedException;
import com.aischolartwin.exception.DisabledAccountException;
import com.aischolartwin.exception.DuplicateEmailException;
import com.aischolartwin.exception.UserNotFoundException;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user authentication, registration, credential verification,
 * and token issuance.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new student or researcher account.
     * Enforces unique email, password hashing, and default role assignment.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.normalizedEmail();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("An account with this email address already exists.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        // New accounts always default to USER role and active status
        User newUser = new User(
            request.fullName().trim(),
            normalizedEmail,
            hashedPassword,
            Role.USER
        );

        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.of(
            token,
            jwtService.getExpirationMs(),
            UserResponse.fromEntity(savedUser)
        );
    }

    /**
     * Authenticates an existing user and issues a signed JWT token.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.normalizedEmail();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new DisabledAccountException("Your account is deactivated. Please contact support.");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.of(
            token,
            jwtService.getExpirationMs(),
            UserResponse.fromEntity(user)
        );
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return UserResponse.fromEntity(user);
    }
}

