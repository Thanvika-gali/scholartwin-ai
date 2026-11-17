-- ==============================================================================
-- AI ScholarTwin — Milestone 7 Schema: Resume & Career Readiness Intelligence
-- Tables: `resumes`, `resume_analyses`, `career_skills`, `job_description_analyses`, `career_actions`
-- ==============================================================================

USE ai_scholartwin;

-- 1. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    processing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    error_message TEXT NULL,
    extracted_text LONGTEXT NULL,
    full_name VARCHAR(150) NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(50) NULL,
    linkedin_url VARCHAR(255) NULL,
    github_url VARCHAR(255) NULL,
    portfolio_url VARCHAR(255) NULL,
    summary TEXT NULL,
    education_json MEDIUMTEXT NULL,
    skills_json MEDIUMTEXT NULL,
    experience_json MEDIUMTEXT NULL,
    projects_json MEDIUMTEXT NULL,
    certifications_json MEDIUMTEXT NULL,
    achievements_json MEDIUMTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumes_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_resumes_user_primary (user_id, is_primary),
    INDEX idx_resumes_user_version (user_id, version_number),
    INDEX idx_resumes_user_status (user_id, processing_status),
    INDEX idx_resumes_user_created (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Resume Analyses Table
CREATE TABLE IF NOT EXISTS resume_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    overall_score INT NOT NULL,
    technical_skills_score INT NOT NULL,
    projects_depth_score INT NOT NULL,
    experience_score INT NOT NULL,
    measurable_results_score INT NOT NULL,
    certifications_score INT NOT NULL,
    structure_readability_score INT NOT NULL,
    role_relevance_score INT NOT NULL,
    strengths_json MEDIUMTEXT NULL,
    weaknesses_json MEDIUMTEXT NULL,
    missing_information_json MEDIUMTEXT NULL,
    actionable_recommendations_json MEDIUMTEXT NULL,
    dimension_breakdown_json MEDIUMTEXT NULL,
    scoring_method VARCHAR(100) NOT NULL DEFAULT 'SCHOLARTWIN_EXPLAINABLE_RUBRIC_V1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_analyses_resume
        FOREIGN KEY (resume_id) REFERENCES resumes (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_resume_analyses_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_resume_analyses_user (user_id, created_at DESC),
    INDEX idx_resume_analyses_resume (resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Career Skills Inventory Table
CREATE TABLE IF NOT EXISTS career_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    proficiency VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
    source VARCHAR(50) NOT NULL DEFAULT 'USER_ENTERED',
    evidence_text VARCHAR(500) NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_career_skills_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT uk_career_skills_user_skill
        UNIQUE (user_id, skill_name),
    INDEX idx_career_skills_user_category (user_id, category),
    INDEX idx_career_skills_user_proficiency (user_id, proficiency)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Job Description Analyses Table
CREATE TABLE IF NOT EXISTS job_description_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NULL,
    job_title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NULL,
    raw_text MEDIUMTEXT NOT NULL,
    target_role VARCHAR(100) NULL,
    experience_required VARCHAR(100) NULL,
    education_required VARCHAR(100) NULL,
    responsibilities_json MEDIUMTEXT NULL,
    match_percentage DOUBLE NOT NULL,
    matched_required_skills_json MEDIUMTEXT NULL,
    missing_required_skills_json MEDIUMTEXT NULL,
    matched_preferred_skills_json MEDIUMTEXT NULL,
    missing_preferred_skills_json MEDIUMTEXT NULL,
    keyword_coverage_json MEDIUMTEXT NULL,
    tailored_suggestions_json MEDIUMTEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_desc_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_job_desc_resume
        FOREIGN KEY (resume_id) REFERENCES resumes (id)
        ON DELETE SET NULL,
    INDEX idx_job_desc_user (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Career Actions Plan Table
CREATE TABLE IF NOT EXISTS career_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    reason_and_evidence TEXT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    related_skill_or_role VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    suggested_deadline DATE NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_career_actions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_career_actions_user_status (user_id, status),
    INDEX idx_career_actions_user_priority (user_id, priority),
    INDEX idx_career_actions_user_deadline (user_id, suggested_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

