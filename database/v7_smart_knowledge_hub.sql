-- =============================================================================
-- AI ScholarTwin — Database Migration v7: Smart Knowledge Hub 2.0
-- =============================================================================

USE ai_scholartwin;

-- 1. Knowledge Folders table
CREATE TABLE IF NOT EXISTS knowledge_folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    icon VARCHAR(50) DEFAULT 'folder',
    color VARCHAR(30) DEFAULT '#178582',
    is_pinned BOOLEAN DEFAULT FALSE,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kf_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_kf_parent FOREIGN KEY (parent_id) REFERENCES knowledge_folders (id) ON DELETE SET NULL,
    INDEX idx_kf_user_parent (user_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Alter documents table to add folder, flags, hash, versions, and intelligence metadata
ALTER TABLE documents
    ADD COLUMN folder_id BIGINT NULL,
    ADD COLUMN is_favorite BOOLEAN DEFAULT FALSE,
    ADD COLUMN is_pinned BOOLEAN DEFAULT FALSE,
    ADD COLUMN is_archived BOOLEAN DEFAULT FALSE,
    ADD COLUMN file_hash VARCHAR(64) NULL,
    ADD COLUMN version_number INT DEFAULT 1,
    ADD COLUMN parent_document_id BIGINT NULL,
    ADD COLUMN document_summary TEXT NULL,
    ADD COLUMN extracted_metadata_json MEDIUMTEXT NULL,
    ADD COLUMN last_viewed_at TIMESTAMP NULL;

-- 3. Alter knowledge_items table to add folder, flags, view timestamp, connected document
ALTER TABLE knowledge_items
    ADD COLUMN folder_id BIGINT NULL,
    ADD COLUMN is_favorite BOOLEAN DEFAULT FALSE,
    ADD COLUMN is_pinned BOOLEAN DEFAULT FALSE,
    ADD COLUMN is_archived BOOLEAN DEFAULT FALSE,
    ADD COLUMN last_viewed_at TIMESTAMP NULL,
    ADD COLUMN connected_document_id BIGINT NULL,
    ADD COLUMN connected_page_number INT NULL;

-- 4. Knowledge Collections table
CREATE TABLE IF NOT EXISTS knowledge_collections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500) NULL,
    color VARCHAR(30) DEFAULT '#178582',
    icon VARCHAR(50) DEFAULT 'collection',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kc_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_kc_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Collection Items mapping table
CREATE TABLE IF NOT EXISTS knowledge_collection_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kci_collection FOREIGN KEY (collection_id) REFERENCES knowledge_collections (id) ON DELETE CASCADE,
    UNIQUE KEY uk_kci_resource (collection_id, resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Resource Tags table
CREATE TABLE IF NOT EXISTS knowledge_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(30) DEFAULT '#178582',
    CONSTRAINT fk_kt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uk_kt_user_name (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Resource Tag Mapping table
CREATE TABLE IF NOT EXISTS knowledge_resource_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id BIGINT NOT NULL,
    CONSTRAINT fk_krt_tag FOREIGN KEY (tag_id) REFERENCES knowledge_tags (id) ON DELETE CASCADE,
    UNIQUE KEY uk_krt_resource (tag_id, resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Resource Connections / Relations table
CREATE TABLE IF NOT EXISTS knowledge_resource_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    relation_type VARCHAR(50) DEFAULT 'RELATED',
    reason VARCHAR(255) NULL,
    is_manual BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_krr_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uk_krr_edge (user_id, source_type, source_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Smart Organization Suggestions table
CREATE TABLE IF NOT EXISTS knowledge_organization_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id BIGINT NOT NULL,
    suggested_folder_id BIGINT NULL,
    suggested_folder_path VARCHAR(255) NULL,
    suggested_subject VARCHAR(100) NULL,
    suggested_semester INT NULL,
    suggested_category VARCHAR(50) NULL,
    suggested_tags_json VARCHAR(1000) NULL,
    confidence_score DOUBLE NOT NULL DEFAULT 0.0,
    reasoning VARCHAR(500) NULL,
    is_ai_generated BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kos_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_kos_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Organization Rules table
CREATE TABLE IF NOT EXISTS knowledge_organization_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rule_name VARCHAR(150) NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    operator VARCHAR(30) NOT NULL,
    condition_value VARCHAR(255) NOT NULL,
    target_folder_id BIGINT NOT NULL,
    suggested_tags VARCHAR(255) NULL,
    auto_move BOOLEAN DEFAULT FALSE,
    priority INT DEFAULT 0,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_kor_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_kor_folder FOREIGN KEY (target_folder_id) REFERENCES knowledge_folders (id) ON DELETE CASCADE,
    INDEX idx_kor_user_enabled (user_id, is_enabled, priority DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. View History table
CREATE TABLE IF NOT EXISTS knowledge_view_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id BIGINT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kvh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_kvh_user_viewed (user_id, viewed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
