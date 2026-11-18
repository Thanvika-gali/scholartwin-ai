package com.aischolartwin.security;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("Public endpoint /api/health should allow unauthenticated access and set security headers")
    void publicEndpointsShouldBeAccessibleWithSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Protected endpoint /api/documents should reject unauthenticated requests with 401 Unauthorized")
    void protectedEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Protected endpoint /api/learning/profile should reject unauthenticated requests with 401")
    void protectedLearningEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/learning/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Protected endpoint /api/intelligence/chat/history should reject unauthenticated requests with 401")
    void protectedChatEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/intelligence/chat/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Protected endpoint should reject invalid bearer token with 401")
    void shouldRejectInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer invalid.malformed.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Protected endpoint should reject expired bearer token with 401")
    void shouldRejectExpiredBearerToken() throws Exception {
        // Create an expired token using negative expiration
        JwtService expiredJwtService = new JwtService(
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
                -10000L
        );
        User testUser = new User("Expired User", "expired@test.com", "hash", Role.USER);
        testUser.setId(9999L);
        String expiredToken = expiredJwtService.generateToken(testUser);

        mockMvc.perform(get("/api/documents")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("FileStorageService should detect and prevent path traversal attacks")
    void shouldDetectAndPreventPathTraversal() {
        assertThrows(InvalidPayloadException.class, () -> {
            fileStorageService.resolve("../../etc/passwd");
        });

        assertThrows(InvalidPayloadException.class, () -> {
            fileStorageService.resolve("..\\..\\windows\\system32\\cmd.exe");
        });

        assertThrows(InvalidPayloadException.class, () -> {
            fileStorageService.resolve("subfolder/file.pdf");
        });
    }
}
