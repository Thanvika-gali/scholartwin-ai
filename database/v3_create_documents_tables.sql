-- ==============================================================================
-- AI ScholarTwin — Milestone 4 Schema: Document Processing & Knowledge Extraction
-- Table Definitions: `documents` and `document_chunks`
-- ==============================================================================

USE ai_scholartwin;

-- 1. Documents Table
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    file_extension VARCHAR(10) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    format VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    page_count INT NULL,
    character_count INT NOT NULL DEFAULT 0,
    chunk_count INT NOT NULL DEFAULT 0,
    ocr_used BOOLEAN NOT NULL DEFAULT FALSE,
    ocr_required BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_documents_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_documents_user_updated (user_id, updated_at DESC),
    INDEX idx_documents_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Document Chunks Table
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    chunk_text MEDIUMTEXT NOT NULL,
    start_page INT NOT NULL,
    end_page INT NOT NULL,
    character_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_chunks_document
        FOREIGN KEY (document_id) REFERENCES documents (id)
        ON DELETE CASCADE,
    INDEX idx_chunks_document_index (document_id, chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

