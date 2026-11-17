-- =============================================================================
-- AI ScholarTwin — Schema Definition: users Table (Milestone 2 Stage 2)
-- =============================================================================
-- In development, Hibernate automatically provisions or updates this schema
-- via 'spring.jpa.hibernate.ddl-auto=update'.
-- This script provides the canonical, non-destructive DDL reference.
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

