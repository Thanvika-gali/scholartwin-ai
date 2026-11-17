-- ==============================================================================
-- AI ScholarTwin — Milestone 6 Schema: DSA & Coding Progress Tracker
-- Tables: `dsa_problems`, `dsa_problem_topics`, `dsa_attempts`, `dsa_revisions`
-- ==============================================================================

USE ai_scholartwin;

-- 1. DSA Problems Table
CREATE TABLE IF NOT EXISTS dsa_problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    problem_url VARCHAR(500) NULL,
    platform VARCHAR(50) NOT NULL DEFAULT 'LEETCODE',
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    primary_topic VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    personal_notes TEXT NULL,
    solution_approach MEDIUMTEXT NULL,
    time_complexity VARCHAR(50) NULL,
    space_complexity VARCHAR(50) NULL,
    revision_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    solved_at TIMESTAMP NULL,
    CONSTRAINT fk_dsa_problems_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_dsa_problems_user_status (user_id, status),
    INDEX idx_dsa_problems_user_topic (user_id, primary_topic),
    INDEX idx_dsa_problems_user_difficulty (user_id, difficulty),
    INDEX idx_dsa_problems_user_updated (user_id, updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. DSA Problem Secondary Topics Table (ElementCollection)
CREATE TABLE IF NOT EXISTS dsa_problem_topics (
    problem_id BIGINT NOT NULL,
    topic VARCHAR(50) NOT NULL,
    PRIMARY KEY (problem_id, topic),
    CONSTRAINT fk_dsa_problem_topics_problem
        FOREIGN KEY (problem_id) REFERENCES dsa_problems (id)
        ON DELETE CASCADE,
    INDEX idx_dsa_problem_topics_topic (topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. DSA Attempts Table
CREATE TABLE IF NOT EXISTS dsa_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    attempt_date DATE NOT NULL,
    outcome VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    time_spent_minutes INT NULL,
    programming_language VARCHAR(50) NULL,
    learning_notes TEXT NULL,
    difficulty_encountered VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dsa_attempts_problem
        FOREIGN KEY (problem_id) REFERENCES dsa_problems (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dsa_attempts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_dsa_attempts_problem (problem_id, attempt_date DESC),
    INDEX idx_dsa_attempts_user_date (user_id, attempt_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. DSA Revisions Table
CREATE TABLE IF NOT EXISTS dsa_revisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    scheduled_date DATE NOT NULL,
    completed_at TIMESTAMP NULL,
    revision_notes TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dsa_revisions_problem
        FOREIGN KEY (problem_id) REFERENCES dsa_problems (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dsa_revisions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_dsa_revisions_user_status_date (user_id, status, scheduled_date),
    INDEX idx_dsa_revisions_problem (problem_id, scheduled_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

