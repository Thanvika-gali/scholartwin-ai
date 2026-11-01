package com.aischolartwin.dto;

import java.time.Instant;

/**
 * Standardized Health Check Response Transfer Object.
 * Uses Java 17 Record for clean, immutable data representation.
 */
public record HealthResponse(
    String status,
    String service,
    String version,
    String timestamp,
    String javaVersion,
    String database
) {
    public static HealthResponse create(String status, String databaseStatus) {
        return new HealthResponse(
            status,
            "AI ScholarTwin Backend",
            "1.0.0",
            Instant.now().toString(),
            System.getProperty("java.version", "17"),
            databaseStatus
        );
    }
}
