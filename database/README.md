# Database Documentation & Scripts

## Overview
AI ScholarTwin uses **MySQL 8.x** as its relational database management system.

- **Database Name:** `ai_scholartwin`
- **Default Port:** `3306`
- **ORM / Persistence:** Spring Data JPA + Hibernate

---

## Local Setup Instructions

### 1. Dedicated User & Database Creation
For security and isolation, do not use the MySQL `root` administrative account for the running application. Create a dedicated application user and grant permissions only on the `ai_scholartwin` database.

Run the following commands in the MySQL client (`mysql -u root -p`):

```sql
-- 1. Create the database with UTF-8 character encoding
CREATE DATABASE IF NOT EXISTS ai_scholartwin
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. Cleanly create the dedicated application user
DROP USER IF EXISTS 'scholartwin_user'@'localhost';
DROP USER IF EXISTS 'scholartwin_user'@'%';

CREATE USER 'scholartwin_user'@'localhost' IDENTIFIED BY 'scholartwin_password';
CREATE USER 'scholartwin_user'@'%' IDENTIFIED BY 'scholartwin_password';

-- 3. Grant privileges strictly on the ai_scholartwin database
GRANT ALL PRIVILEGES ON ai_scholartwin.* TO 'scholartwin_user'@'localhost';
GRANT ALL PRIVILEGES ON ai_scholartwin.* TO 'scholartwin_user'@'%';

-- 4. Apply privilege changes
FLUSH PRIVILEGES;
```

---

## Automated Setup & Verification (PowerShell)

You can run the automated script from the repository root to verify connectivity or run initialization:

```powershell
powershell -ExecutionPolicy Bypass -File .\database\setup-db.ps1
```

- If `scholartwin_user` is already working, it will verify and output connection diagnostics.
- If user recreation or administrative initialization is required, run with `-ForceAdmin` or when prompted:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\database\setup-db.ps1 -ForceAdmin
  ```
  It securely prompts for the MySQL `root` password (input is masked), executes `init.sql`, and verifies access.

---

## Environment Variables Configuration

The backend connects to MySQL using standard environment variables:

| Variable | Description | Local Development Example |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC Connection URL | `jdbc:mysql://localhost:3306/ai_scholartwin?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `scholartwin_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database user password | `scholartwin_password` |

---

## Schema Evolution
- **Milestone 1:** Database connectivity and health check (`GET /api/health`).
- **Milestone 2:** User entity, roles, and authentication (`v1_create_users_table.sql`).
- **Milestone 3:** Knowledge hub notes and categories (`v2_create_notes_table.sql`).
- **Milestone 4:** Document processing, pages, and chunks (`v3_create_documents_tables.sql`).
- **Milestone 6:** DSA & Coding Progress Tracker (`v4_create_dsa_tables.sql`):
  - `dsa_problems`: Core problem tracker entity (title, URL, platform, difficulty, topic, complexities, notes, status).
  - `dsa_problem_topics`: Multi-topic join table for secondary topic patterns.
  - `dsa_attempts`: Problem-solving attempt logs (date, outcome, duration, language, takeaways).
  - `dsa_revisions`: Spaced repetition revision queue (scheduled date, completion timestamp, notes).
- **Milestone 7:** Resume & Career Readiness Intelligence (`v5_create_career_tables.sql`):
  - `resumes`: Multi-version resume repository (PDF/DOCX), magic-byte validation, version numbers, primary active flag, parsing status, extracted raw text, structured JSON cache.
  - `resume_analyses`: 100-point transparent rubric diagnostic evaluations across 7 core dimensions, strengths, weaknesses, missing information, and actionable recommendations.
  - `career_skills`: Categorized technical and soft skills inventory across 10 disciplines, proficiency ratings, verified badges, and evidence notes.
  - `job_description_analyses`: Job specification diagnostic matcher, match %, matched/missing required skills, preferred qualifications, keyword coverage, and tailored suggestions.
  - `career_actions`: Personalized career action plan items, priority levels, deadlines, status lifecycle, and cross-module DSA tracker practice integration.


