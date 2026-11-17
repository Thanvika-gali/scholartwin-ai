package com.aischolartwin.dto;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid RegisterRequest should produce no constraint violations")
    void validRegisterRequestShouldPass() {
        RegisterRequest request = new RegisterRequest("Marie Curie", "marie.curie@sorbonne.fr", "SecurePassword123!");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Expected no violations for valid registration request");
    }

    @Test
    @DisplayName("RegisterRequest with invalid data should trigger bean validation errors")
    void invalidRegisterRequestShouldFailValidation() {
        // Blank name (violates @NotBlank and @Size), invalid email, short password (< 8 chars)
        RegisterRequest request = new RegisterRequest("", "invalid-email", "short");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3);

        Set<String> violatedProperties = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(violatedProperties.contains("fullName"));
        assertTrue(violatedProperties.contains("email"));
        assertTrue(violatedProperties.contains("password"));
    }

    @Test
    @DisplayName("RegisterRequest toString() should mask the password")
    void registerRequestToStringShouldMaskPassword() {
        RegisterRequest request = new RegisterRequest("Nikola Tesla", "tesla@wardenclyffe.org", "SuperSecret99#");
        String stringRep = request.toString();

        assertFalse(stringRep.contains("SuperSecret99#"));
        assertTrue(stringRep.contains("[PROTECTED]"));
    }

    @Test
    @DisplayName("Valid LoginRequest should produce no constraint violations")
    void validLoginRequestShouldPass() {
        LoginRequest request = new LoginRequest("tesla@wardenclyffe.org", "SuperSecret99#");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LoginRequest with blank fields or invalid email should fail validation")
    void invalidLoginRequestShouldFailValidation() {
        LoginRequest request = new LoginRequest("not-an-email", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("LoginRequest toString() should mask the password")
    void loginRequestToStringShouldMaskPassword() {
        LoginRequest request = new LoginRequest("user@example.com", "PlaintextPassword!");
        String stringRep = request.toString();

        assertFalse(stringRep.contains("PlaintextPassword!"));
        assertTrue(stringRep.contains("[PROTECTED]"));
    }

    @Test
    @DisplayName("UserResponse must map fields from User entity and have no password field")
    void userResponseShouldProjectUserWithoutSensitiveFields() {
        User user = new User("Rosalind Franklin", "rosalind@kcl.ac.uk", "secret_hashed_password", Role.USER);
        user.setId(101L);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserResponse response = UserResponse.fromEntity(user);

        assertNotNull(response);
        assertEquals(101L, response.id());
        assertEquals("Rosalind Franklin", response.fullName());
        assertEquals("rosalind@kcl.ac.uk", response.email());
        assertEquals(Role.USER, response.role());
        assertTrue(response.isActive());
        assertEquals(now, response.createdAt());
        assertEquals(now, response.updatedAt());

        // Reflection check: verify UserResponse has NO password or passwordHash field
        Field[] fields = UserResponse.class.getDeclaredFields();
        boolean hasSensitiveField = Arrays.stream(fields)
                .map(Field::getName)
                .anyMatch(name -> name.toLowerCase().contains("password"));
        assertFalse(hasSensitiveField, "UserResponse must strictly omit password fields");
    }
}
