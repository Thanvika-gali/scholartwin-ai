-- =============================================================================
-- AI ScholarTwin — Database Migration v10: Performance Optimization & Security
-- =============================================================================

USE ai_scholartwin;

DROP PROCEDURE IF EXISTS AddIndexSafely;

DELIMITER $$
CREATE PROCEDURE AddIndexSafely(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_cols VARCHAR(255)
)
BEGIN
    DECLARE idx_exists INT DEFAULT 0;
    SELECT COUNT(1) INTO idx_exists 
    FROM information_schema.statistics 
    WHERE table_schema = DATABASE() 
      AND table_name = p_table 
      AND index_name = p_index;

    IF idx_exists = 0 THEN
        SET @stmt = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` ', p_cols);
        PREPARE s FROM @stmt;
        EXECUTE s;
        DEALLOCATE PREPARE s;
    END IF;
END$$
DELIMITER ;

-- 1. Document & Chunks Performance
CALL AddIndexSafely('document_chunks', 'idx_chunks_doc_chunk', '(document_id, chunk_index)');
CALL AddIndexSafely('documents', 'idx_docs_user_status', '(user_id, status)');
CALL AddIndexSafely('documents', 'idx_docs_user_folder', '(user_id, folder_id)');
CALL AddIndexSafely('documents', 'idx_docs_user_archived', '(user_id, is_archived)');

-- 2. DSA Tracker Performance Indexes
CALL AddIndexSafely('dsa_problems', 'idx_dsa_prob_user_diff', '(user_id, difficulty)');
CALL AddIndexSafely('dsa_problems', 'idx_dsa_prob_user_status', '(user_id, status)');
CALL AddIndexSafely('dsa_attempts', 'idx_dsa_attempts_user_date', '(user_id, attempt_date)');

-- 3. Career & Resume Performance Indexes
CALL AddIndexSafely('resumes', 'idx_resumes_user_primary', '(user_id, is_primary)');
CALL AddIndexSafely('career_skills', 'idx_skills_user_cat', '(user_id, category)');
CALL AddIndexSafely('career_actions', 'idx_actions_user_status', '(user_id, status)');

-- 4. Knowledge Hub Indexes
CALL AddIndexSafely('knowledge_items', 'idx_ki_user_folder', '(user_id, folder_id)');
CALL AddIndexSafely('knowledge_items', 'idx_ki_user_updated', '(user_id, updated_at)');
CALL AddIndexSafely('knowledge_folders', 'idx_kf_user_pinned', '(user_id, is_pinned)');

-- 5. Learning & Memory Analytics Indexes
CALL AddIndexSafely('planner_tasks', 'idx_planner_user_status_date', '(user_id, status, scheduled_date)');
CALL AddIndexSafely('study_sessions', 'idx_sessions_user_date', '(user_id, session_date)');
CALL AddIndexSafely('user_memories', 'idx_memories_lookup', '(user_id, status, is_pinned)');

DROP PROCEDURE IF EXISTS AddIndexSafely;
