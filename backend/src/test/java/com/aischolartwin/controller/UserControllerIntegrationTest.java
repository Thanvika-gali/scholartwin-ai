package com.aischolartwin.controller;

import com.aischolartwin.dto.UpdateProfileRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

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
    @DisplayName("GET /api/users/me without authentication returns 401 Unauthorized")
    void getProfileWithoutAuthShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/users/me with valid JWT returns authenticated user profile")
    void getProfileWithAuthShouldReturnUser() throws Exception {
        User user = new User("Claude Shannon", "shannon@mit.edu", passwordEncoder.encode("InfoTheory1948!"), Role.USER);
        user = userRepository.saveAndFlush(user);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Claude Shannon"))
                .andExpect(jsonPath("$.email").value("shannon@mit.edu"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/users/me updates full name and returns updated profile")
    void updateProfileShouldUpdateName() throws Exception {
        User user = new User("Edsger Dijkstra", "dijkstra@utexas.edu", passwordEncoder.encode("ShortestPath1956!"), Role.USER);
        user = userRepository.saveAndFlush(user);
        String token = jwtService.generateToken(user);

        UpdateProfileRequest updateRequest = new UpdateProfileRequest("Edsger W. Dijkstra");

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Edsger W. Dijkstra"))
                .andExpect(jsonPath("$.email").value("dijkstra@utexas.edu"));
    }

    @Test
    @DisplayName("PUT /api/users/me with blank name returns 400 Bad Request")
    void updateProfileWithBlankNameShouldReturn400() throws Exception {
        User user = new User("Leslie Lamport", "lamport@paxos.org", passwordEncoder.encode("PaxosAlgorithm1989!"), Role.USER);
        user = userRepository.saveAndFlush(user);
        String token = jwtService.generateToken(user);

        UpdateProfileRequest invalidRequest = new UpdateProfileRequest("");

        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.fullName").exists());
    }
}

