-- =============================================================================
-- AI ScholarTwin — Database Migration v8: Personalized Learning & Revision Engine
-- =============================================================================

USE ai_scholartwin;

-- 1. Learning Profiles
CREATE TABLE IF NOT EXISTS learning_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    target_role VARCHAR(150) NOT NULL DEFAULT 'Software Development Engineer',
    current_level VARCHAR(50) NOT NULL DEFAULT 'INTERMEDIATE',
    preferred_learning_style VARCHAR(50) NOT NULL DEFAULT 'BALANCED',
    target_pace VARCHAR(50) NOT NULL DEFAULT 'MODERATE',
    daily_study_goal_minutes INT NOT NULL DEFAULT 60,
    preferred_study_time VARCHAR(50) DEFAULT 'EVENING',
    active_streak_days INT NOT NULL DEFAULT 0,
    longest_streak_days INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Learning Roadmaps
CREATE TABLE IF NOT EXISTS learning_roadmaps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_role VARCHAR(150) NOT NULL,
    estimated_duration_weeks INT NOT NULL DEFAULT 12,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    progress_percentage DOUBLE NOT NULL DEFAULT 0.0,
    target_completion_date DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lr_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_roadmaps_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Roadmap Phases
CREATE TABLE IF NOT EXISTS roadmap_phases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    phase_order INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    estimated_weeks INT NOT NULL DEFAULT 2,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rp_roadmap FOREIGN KEY (roadmap_id) REFERENCES learning_roadmaps (id) ON DELETE CASCADE,
    INDEX idx_phases_roadmap_order (roadmap_id, phase_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Learning Tasks
CREATE TABLE IF NOT EXISTS learning_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phase_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    estimated_minutes INT NOT NULL DEFAULT 45,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lt_phase FOREIGN KEY (phase_id) REFERENCES roadmap_phases (id) ON DELETE CASCADE,
    INDEX idx_tasks_phase_status (phase_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Study Sessions
CREATE TABLE IF NOT EXISTS study_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30,
    session_date DATE NOT NULL,
    notes TEXT NULL,
    focus_rating INT DEFAULT 4,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ss_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_study_sessions_user_date (user_id, session_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Daily Study Planner Tasks
CREATE TABLE IF NOT EXISTS planner_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL DEFAULT 'STUDY',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    scheduled_date DATE NOT NULL,
    estimated_minutes INT NOT NULL DEFAULT 45,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    source_reference_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_planner_tasks_user_date (user_id, scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Spaced Revision Items
CREATE TABLE IF NOT EXISTS spaced_revision_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    reference_id BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    difficulty VARCHAR(50) DEFAULT 'MEDIUM',
    interval_days INT NOT NULL DEFAULT 1,
    repetition_count INT NOT NULL DEFAULT 0,
    ease_factor DOUBLE NOT NULL DEFAULT 2.5,
    scheduled_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sri_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_spaced_revisions_user_date_status (user_id, scheduled_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Revision Attempts
CREATE TABLE IF NOT EXISTS revision_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    rating VARCHAR(50) NOT NULL,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    feedback TEXT NULL,
    CONSTRAINT fk_ra_item FOREIGN KEY (item_id) REFERENCES spaced_revision_items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Learning Assessments
CREATE TABLE IF NOT EXISTS learning_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    assessment_type VARCHAR(50) NOT NULL DEFAULT 'QUIZ',
    difficulty VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    total_questions INT NOT NULL DEFAULT 5,
    passing_score_percentage INT NOT NULL DEFAULT 70,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_la_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_assessments_user_topic (user_id, topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Assessment Questions
CREATE TABLE IF NOT EXISTS assessment_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_order INT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    options_json TEXT NULL,
    correct_answer TEXT NOT NULL,
    explanation TEXT NULL,
    CONSTRAINT fk_aq_assessment FOREIGN KEY (assessment_id) REFERENCES learning_assessments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Assessment Attempts
CREATE TABLE IF NOT EXISTS assessment_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score_percentage DOUBLE NOT NULL,
    passed BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    answers_json MEDIUMTEXT NULL,
    CONSTRAINT fk_aa_assessment FOREIGN KEY (assessment_id) REFERENCES learning_assessments (id) ON DELETE CASCADE,
    CONSTRAINT fk_aa_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_assessment_attempts_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Learning Goals
CREATE TABLE IF NOT EXISTS learning_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_title VARCHAR(255) NOT NULL,
    target_metric VARCHAR(100) NOT NULL,
    target_value INT NOT NULL,
    current_value INT NOT NULL DEFAULT 0,
    deadline DATE NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lg_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Learning Resource Associations
CREATE TABLE IF NOT EXISTS learning_resource_associations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    url VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lra_task FOREIGN KEY (task_id) REFERENCES learning_tasks (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
