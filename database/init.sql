-- =============================================================================
-- AI ScholarTwin — Database Initialization Script (Milestone 1)
-- =============================================================================
-- Run this script using an administrative MySQL account:
-- mysql -u root -p < database/init.sql
-- =============================================================================

-- 1. Create dedicated database with UTF-8 character encoding
CREATE DATABASE IF NOT EXISTS ai_scholartwin
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. Create dedicated non-root application user
CREATE USER IF NOT EXISTS 'scholartwin_user'@'localhost' IDENTIFIED BY 'scholartwin_password';

-- 3. Grant application-level permissions only on the ai_scholartwin database
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE
  ON ai_scholartwin.* TO 'scholartwin_user'@'localhost';

-- 4. Flush privileges to apply changes immediately
FLUSH PRIVILEGES;

-- Verify creation
SELECT User, Host FROM mysql.user WHERE User = 'scholartwin_user';
