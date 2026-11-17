-- =============================================================================
-- AI ScholarTwin — Schema Definition: knowledge_items Table (Milestone 3)
-- =============================================================================
-- In development, Hibernate automatically provisions and updates this schema
-- via 'spring.jpa.hibernate.ddl-auto=update'.
-- This script provides the canonical, non-destructive DDL reference.
-- =============================================================================

CREATE TABLE IF NOT EXISTS knowledge_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    url VARCHAR(1000),
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL DEFAULT 'General',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_knowledge_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_ki_user_id (user_id),
    INDEX idx_ki_user_updated (user_id, updated_at DESC),
    INDEX idx_ki_user_type (user_id, type),
    INDEX idx_ki_user_category (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

