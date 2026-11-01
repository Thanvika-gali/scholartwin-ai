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

-- 2. Create the dedicated application user
CREATE USER IF NOT EXISTS 'scholartwin_user'@'localhost' IDENTIFIED BY 'scholartwin_password';

-- 3. Grant necessary privileges to the application database
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE
  ON ai_scholartwin.* TO 'scholartwin_user'@'localhost';

-- 4. Apply privilege changes
FLUSH PRIVILEGES;
```

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
- In **Milestone 1**, only database connectivity is verified. Application entity tables will be created in subsequent milestones using structured migrations.
