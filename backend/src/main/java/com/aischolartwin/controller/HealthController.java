package com.aischolartwin.controller;

import com.aischolartwin.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

/**
 * Controller providing service health and connectivity status.
 * Exposed at /api/health.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final Optional<DataSource> dataSource;

    @Autowired
    public HealthController(Optional<DataSource> dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        String dbStatus = checkDatabaseConnection();
        HealthResponse response = HealthResponse.create("UP", dbStatus);
        return ResponseEntity.ok(response);
    }

    private String checkDatabaseConnection() {
        if (dataSource.isEmpty()) {
            return "NOT_CONFIGURED";
        }

        try (Connection connection = dataSource.get().getConnection()) {
            if (connection.isValid(2)) {
                return "CONNECTED";
            } else {
                return "UNHEALTHY";
            }
        } catch (Exception ex) {
            return "DISCONNECTED: " + ex.getMessage();
        }
    }
}
