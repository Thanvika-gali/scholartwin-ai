-- =============================================================================
-- AI ScholarTwin — Database Migration v9: AI Insights, Memory & Analytics
-- =============================================================================

USE ai_scholartwin;

-- 1. User Memories
CREATE TABLE IF NOT EXISTS user_memories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    memory_type VARCHAR(30) NOT NULL,
    importance VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    summary VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    provenance VARCHAR(30) NOT NULL DEFAULT 'SYSTEM_INFERRED',
    source_type VARCHAR(30) NOT NULL DEFAULT 'CONVERSATION',
    source_reference_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    access_count INT NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_um_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_memories_user_status (user_id, status),
    INDEX idx_memories_user_cat_status (user_id, category, status),
    INDEX idx_memories_user_pinned (user_id, is_pinned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Memory Preferences
CREATE TABLE IF NOT EXISTS memory_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    memory_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    allow_dsa_extraction BOOLEAN NOT NULL DEFAULT TRUE,
    allow_career_extraction BOOLEAN NOT NULL DEFAULT TRUE,
    allow_academic_extraction BOOLEAN NOT NULL DEFAULT TRUE,
    allow_learning_style_extraction BOOLEAN NOT NULL DEFAULT TRUE,
    retention_period_days INT NOT NULL DEFAULT 365,
    auto_prune_unaccessed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. AI Insights
CREATE TABLE IF NOT EXISTS ai_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    importance VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    actionable_recommendation TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_insights_user_status (user_id, status),
    INDEX idx_insights_user_cat (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. AI Insight Evidences
CREATE TABLE IF NOT EXISTS ai_insight_evidences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insight_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_reference_id BIGINT NULL,
    evidence_text TEXT NOT NULL,
    CONSTRAINT fk_aie_insight FOREIGN KEY (insight_id) REFERENCES ai_insights (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. AI Recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rationale TEXT NOT NULL,
    action_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    action_target_url VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_recommendations_user_status (user_id, status),
    INDEX idx_recommendations_user_priority (user_id, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Knowledge Graph Nodes
CREATE TABLE IF NOT EXISTS ai_knowledge_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    domain VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    proficiency_score DOUBLE NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_akn_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_nodes_user_domain (user_id, domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Knowledge Graph Relationships
CREATE TABLE IF NOT EXISTS ai_knowledge_relationships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_node_id BIGINT NOT NULL,
    target_node_id BIGINT NOT NULL,
    relation_type VARCHAR(50) NOT NULL,
    weight DOUBLE NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_akr_source FOREIGN KEY (source_node_id) REFERENCES ai_knowledge_nodes (id) ON DELETE CASCADE,
    CONSTRAINT fk_akr_target FOREIGN KEY (target_node_id) REFERENCES ai_knowledge_nodes (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. AI Interaction Logs
CREATE TABLE IF NOT EXISTS ai_interaction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    response_text MEDIUMTEXT NOT NULL,
    intent VARCHAR(50) NOT NULL,
    grounding_mode VARCHAR(50) NOT NULL,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ail_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_ai_logs_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
