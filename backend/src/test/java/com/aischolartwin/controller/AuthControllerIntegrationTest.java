package com.aischolartwin.controller;

import com.aischolartwin.dto.LoginRequest;
import com.aischolartwin.dto.RegisterRequest;
import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/auth/register creates account, hashes password, and returns 201 with JWT")
    void shouldRegisterAccount() throws Exception {
        RegisterRequest request = new RegisterRequest("Barbara Liskov", "liskov@mit.edu", "LiskovSubPrinciple1!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.fullName").value("Barbara Liskov"))
                .andExpect(jsonPath("$.user.email").value("liskov@mit.edu"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email returns 409 Conflict")
    void shouldRejectDuplicateEmail() throws Exception {
        User existing = new User("Margaret Hamilton", "margaret@apollo.nasa.gov", passwordEncoder.encode("Apollo11Moon!"), Role.USER);
        userRepository.saveAndFlush(existing);

        RegisterRequest duplicate = new RegisterRequest("Margaret Clone", "MARGARET@APOLLO.NASA.GOV", "NewPassword123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/auth/login authenticates valid user and returns 200 with JWT")
    void shouldLoginSuccessfully() throws Exception {
        User user = new User("Tim Berners-Lee", "tim@w3c.org", passwordEncoder.encode("WorldWideWeb1989!"), Role.USER);
        userRepository.saveAndFlush(user);

        LoginRequest loginRequest = new LoginRequest("tim@w3c.org", "WorldWideWeb1989!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("tim@w3c.org"));
    }

    @Test
    @DisplayName("POST /api/auth/login with bad password returns 401 Unauthorized")
    void shouldRejectBadPassword() throws Exception {
        User user = new User("Vint Cerf", "vint@isoc.org", passwordEncoder.encode("TcpIpProtocol1974!"), Role.USER);
        userRepository.saveAndFlush(user);

        LoginRequest badLogin = new LoginRequest("vint@isoc.org", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    @DisplayName("GET /api/auth/me without token returns 401 Unauthorized")
    void shouldRejectUnauthenticatedMe() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me with valid Bearer token returns 200 with current user profile")
    void shouldReturnCurrentUserWithValidToken() throws Exception {
        User user = new User("John von Neumann", "neumann@ias.edu", passwordEncoder.encode("GameTheory1944!"), Role.USER);
        user = userRepository.saveAndFlush(user);

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John von Neumann"))
                .andExpect(jsonPath("$.email").value("neumann@ias.edu"));
    }

    @Test
    @DisplayName("POST /api/auth/logout returns 200 OK")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully."));
    }
}

