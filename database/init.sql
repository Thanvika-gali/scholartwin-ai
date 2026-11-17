-- =============================================================================
-- AI ScholarTwin — Database Initialization Script (Milestone 2)
-- =============================================================================
-- Run this script using an administrative MySQL account (e.g., via MySQL Workbench):
-- =============================================================================

-- 1. Create dedicated database with UTF-8 character encoding
CREATE DATABASE IF NOT EXISTS ai_scholartwin
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. Re-create dedicated application user cleanly
-- Dropping prior user instances prevents the MySQL "CREATE USER IF NOT EXISTS"
-- silent failure where existing password hashes are not updated.
DROP USER IF EXISTS 'scholartwin_user'@'localhost';
DROP USER IF EXISTS 'scholartwin_user'@'%';

-- Create for both localhost (socket/local) and % (IPv4/IPv6/TCP) to prevent host-matching errors
CREATE USER 'scholartwin_user'@'localhost' IDENTIFIED BY 'scholartwin_password';
CREATE USER 'scholartwin_user'@'%' IDENTIFIED BY 'scholartwin_password';

-- 3. Grant application-level permissions strictly on the ai_scholartwin database only
GRANT ALL PRIVILEGES ON ai_scholartwin.* TO 'scholartwin_user'@'localhost';
GRANT ALL PRIVILEGES ON ai_scholartwin.* TO 'scholartwin_user'@'%';

-- 4. Flush privileges to apply changes immediately
FLUSH PRIVILEGES;

-- 5. Verification query
SELECT User, Host, plugin FROM mysql.user WHERE User = 'scholartwin_user';
