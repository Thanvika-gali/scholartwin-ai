-- ==============================================================================
-- AI ScholarTwin — Database Migration v6
-- Expand User Profile & Comprehensive SaaS Settings Center
-- ==============================================================================

USE ai_scholartwin;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS institution VARCHAR(150) NULL AFTER role,
    ADD COLUMN IF NOT EXISTS degree VARCHAR(100) NULL AFTER institution,
    ADD COLUMN IF NOT EXISTS graduation_year INT NULL AFTER degree,
    ADD COLUMN IF NOT EXISTS academic_year VARCHAR(30) NULL AFTER graduation_year,
    ADD COLUMN IF NOT EXISTS current_semester INT NULL AFTER academic_year,
    ADD COLUMN IF NOT EXISTS bio TEXT NULL AFTER current_semester,
    ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255) NULL AFTER bio,
    ADD COLUMN IF NOT EXISTS github_url VARCHAR(255) NULL AFTER avatar_url,
    ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(255) NULL AFTER github_url,
    ADD COLUMN IF NOT EXISTS portfolio_url VARCHAR(255) NULL AFTER linkedin_url,
    ADD COLUMN IF NOT EXISTS academic_preferences TEXT NULL AFTER portfolio_url,
    ADD COLUMN IF NOT EXISTS ai_preferences TEXT NULL AFTER academic_preferences,
    ADD COLUMN IF NOT EXISTS notification_preferences TEXT NULL AFTER ai_preferences,
    ADD COLUMN IF NOT EXISTS theme_preference VARCHAR(20) DEFAULT 'SYSTEM' AFTER notification_preferences;

