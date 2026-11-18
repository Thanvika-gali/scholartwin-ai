# AI ScholarTwin — Deployment & Operations Guide

Comprehensive guide for local development, production builds, and containerized Docker deployments of the **AI ScholarTwin** academic intelligence and career readiness platform.

---

## 1. Prerequisites & System Requirements

| Component | Minimum Specification | Recommended Specification |
| :--- | :--- | :--- |
| **Operating System** | Windows 10/11 64-bit, Ubuntu 22.04 LTS, macOS 13+ | Windows 11 Pro / Ubuntu 24.04 LTS |
| **CPU** | 4 Cores (x86_64) | 8 Cores (for parallel test execution & OCR) |
| **Memory (RAM)** | 8 GB | 16 GB+ |
| **Disk Storage** | 10 GB available SSD space | 25 GB+ SSD space (for models, DB & documents) |
| **Network** | Outbound access on Port 443 | Access to Google Gemini Generative Language API |

---

## 2. Required Software Stack

Ensure the following tools are installed and present on your system `PATH`:

- **Java Development Kit (JDK):** Version 17 (Eclipse Temurin 17 or Oracle OpenJDK 17)
  ```powershell
  java -version
  ```
- **Node.js & npm:** Node.js v20 LTS or v22 LTS with npm v10+
  ```powershell
  node -v
  npm -v
  ```
- **MySQL Database Server:** MySQL 8.0.x Community Server
  ```powershell
  mysql --version
  ```
- **Tesseract OCR (Optional for scanned PDF OCR):** Tesseract 5.x
  ```powershell
  tesseract --version
  ```
- **Docker & Docker Compose (Optional for containerized run):** Docker Desktop v24+
  ```powershell
  docker --version
  docker compose version
  ```

---

## 3. Environment Variables Configuration

Copy the template `.env.example` in the project root to create your operational `.env`:

```powershell
Copy-Item .env.example .env
```

### Complete Environment Specification

```env
# ------------------------------------------------------------------------------
# Server & Active Profiles
# ------------------------------------------------------------------------------
PORT=8080
SPRING_PROFILES_ACTIVE=prod

# ------------------------------------------------------------------------------
# MySQL 8 Database Configuration
# ------------------------------------------------------------------------------
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ai_scholartwin?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=scholartwin_user
SPRING_DATASOURCE_PASSWORD=scholartwin_password

# ------------------------------------------------------------------------------
# JWT Security
# ------------------------------------------------------------------------------
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

# ------------------------------------------------------------------------------
# CORS Allowed Origins (Comma-separated)
# ------------------------------------------------------------------------------
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:80,http://localhost

# ------------------------------------------------------------------------------
# Document Processing Limits (100MB Academic Documents, 20MB Resumes)
# ------------------------------------------------------------------------------
DOCUMENT_STORAGE_DIR=uploads/documents
DOCUMENT_MAX_FILE_SIZE=100MB
DOCUMENT_MAX_REQUEST_SIZE=110MB
DOCUMENT_MAX_FILE_SIZE_BYTES=104857600
DOCUMENT_MAX_PAGE_COUNT=200
DOCUMENT_CHUNK_MAX_SIZE=1000
DOCUMENT_CHUNK_OVERLAP=150
RESUME_MAX_FILE_SIZE_BYTES=20971520

# ------------------------------------------------------------------------------
# OCR (Tesseract) Configuration
# ------------------------------------------------------------------------------
OCR_ENABLED=true
TESSERACT_PATH=tesseract
OCR_LANGUAGE=eng

# ------------------------------------------------------------------------------
# Gemini API Key (Grounding & Reasoning)
# ------------------------------------------------------------------------------
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.5-flash

# ------------------------------------------------------------------------------
# Frontend Client Configuration
# ------------------------------------------------------------------------------
VITE_BACKEND_URL=http://localhost:8080
VITE_API_BASE_URL=
```

---

## 4. Database Setup

Log into MySQL as an administrative user (`root`) and execute `database/init.sql`:

```powershell
# Windows PowerShell
mysql -u root -p < database/init.sql
```

This performs the following:
1. Creates the dedicated database `ai_scholartwin` with `utf8mb4` character set and `utf8mb4_unicode_ci` collation.
2. Creates the dedicated application user `scholartwin_user` identified by `scholartwin_password`.
3. Grants `ALL PRIVILEGES` on `ai_scholartwin.*` to `scholartwin_user` from both `localhost` and `%`.

---

## 5. Database Migrations

The database migrations are structured sequentially in the `database/` folder:

| Script | Purpose |
| :--- | :--- |
| `v1_create_users_table.sql` | Users table and authentication schema |
| `v2_create_knowledge_items_table.sql` | Knowledge items and notes |
| `v3_create_documents_tables.sql` | Documents, status, and chunk storage |
| `v4_create_dsa_tables.sql` | DSA problems, topics, and attempts |
| `v5_create_career_tables.sql` | Resumes, career skills, actions, and job matching |
| `v6_expand_user_profile_and_settings.sql` | Extended student profiles and UI preferences |
| `v7_smart_knowledge_hub.sql` | Knowledge folders, collections, rules, tags, and relations |
| `v8_learning_revision_engine.sql` | Roadmaps, phases, tasks, sessions, and assessments |
| `v9_ai_insights_memory.sql` | User memories, preferences, insights, and recommendations |
| `v10_performance_and_security.sql` | Composite query indexes and security constraints |

To run all migrations sequentially:

```powershell
# Windows PowerShell
Get-ChildItem -Path database\v*.sql | Sort-Object Name | ForEach-Object {
    Write-Host "Applying $($_.Name)..."
    mysql -u scholartwin_user -pscholartwin_password -D ai_scholartwin -e "source $($_.FullName)"
}
```

> **Note:** Spring Boot JPA is also configured with `spring.jpa.hibernate.ddl-auto=update`, which automatically synchronizes and verifies entity schemas on startup.

---

## 6. Local Development Startup (Windows PowerShell)

### Step 1: Start the Backend (Port 8080)
Open a dedicated PowerShell terminal:
```powershell
cd E:\projects\scholartwin-ai\backend
.\mvnw.cmd spring-boot:run
```
Expected output:
```text
Started AiScholartwinApplication in 5.2 seconds (process running for 6.1)
```

### Step 2: Start the Frontend (Port 5173)
Open a second PowerShell terminal:
```powershell
cd E:\projects\scholartwin-ai\frontend
npm run dev
```
Expected output:
```text
  VITE v8.3.5  ready in 182 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

Open `http://localhost:5173` in your browser. All API requests (`/api/*`) are proxied automatically to `http://localhost:8080`.

---

## 7. Production Build

### Backend Production Build
```powershell
cd E:\projects\scholartwin-ai\backend
.\mvnw.cmd clean package -DskipTests
```
The executable production JAR is generated at:
`backend/target/backend-0.0.1-SNAPSHOT.jar`

Run the production JAR standalone:
```powershell
java -jar -Dspring.profiles.active=prod target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Production Build
```powershell
cd E:\projects\scholartwin-ai\frontend
npm run build
```
The optimized production bundle is compiled to `frontend/dist/` in under 1 second.

---

## 8. Docker & Docker Compose Deployment

If deploying to a server or developer workstation with Docker Desktop installed:

### Step 1: Review Configuration
Ensure your `.env` contains valid production secrets.

### Step 2: Build and Launch Containers
```powershell
docker compose up -d --build
```

### Step 3: Verify Running Services
```powershell
docker compose ps
```
Output should show:
- `scholartwin-mysql`: Running on internal network (Port 3306 on loopback `127.0.0.1:3306`)
- `scholartwin-backend`: Running on internal network (Port 8080 exposed)
- `scholartwin-frontend`: Running on port 80 (Nginx serving SPA and proxying `/api` -> `backend:8080`)

### Step 4: Access Application
Navigate to `http://localhost` (Port 80).

To stop the containers:
```powershell
docker compose down
```
To stop and remove persistent volumes (WARNING: wipes container data):
```powershell
docker compose down -v
```

---

## 9. Persistent File Storage Architecture

Uploaded documents and parsed resumes are stored outside the web root to prevent arbitrary code execution:

- **Local Storage Path:** `E:\projects\scholartwin-ai\backend\uploads\documents\`
- **Container Storage Path:** `/app/uploads/documents/` (mounted via Docker volume `scholartwin_uploads_data`)
- **Naming Strategy:** Secure server-generated UUIDs (`UUID.randomUUID() + "." + safeExtension`).
- **Path Traversal Defense:** `FileStorageService.resolve(...)` validates that the canonical path strictly starts within the configured root directory and rejects `..`, `/`, and `\`.
- **Cascade Cleanup:** Deleting a document removes its physical file from disk and deletes associated database chunk records.

---

## 10. Gemini API Configuration & Grounding

AI ScholarTwin integrates Google's latest `gemini-2.5-flash` model for intelligent synthesis:

1. Obtain an API key from [Google AI Studio](https://aistudio.google.com/).
2. Set `GEMINI_API_KEY=your_key` in `.env` or system environment.
3. If no key is set or the API experiences network latency, AI ScholarTwin's **Self-Contained Academic Reasoning Engine** activates seamlessly:
   - Grounded responses using verified database metrics (DSA solve count, retention rates, ATS resume gaps).
   - Zero hallucinations; explicitly indicates `[Notice: Operating via ScholarTwin Academic Engine]`.

---

## 11. Health-Check & Readiness Verification

AI ScholarTwin provides Spring Boot Actuator and application-level health endpoints:

### 1. Application Health
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/health -Method GET
```
Response:
```json
{
  "status": "UP",
  "service": "AI ScholarTwin Backend",
  "version": "1.0.0",
  "timestamp": "2026-11-18T18:20:00Z",
  "javaVersion": "17.0.19",
  "database": "UP"
}
```

### 2. Actuator Probes
```powershell
Invoke-RestMethod -Uri http://localhost:8080/actuator/health -Method GET
```
Response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

---

## 12. Troubleshooting Guide

### Issue A: MySQL Connection Refused (`CommunicationsException`)
- **Cause:** MySQL service is not running or credentials in `.env` differ from `init.sql`.
- **Fix:**
  ```powershell
  # Check Windows MySQL service
  Get-Service -Name MySQL*
  # If stopped:
  Start-Service -Name MySQL80
  ```

### Issue B: Port Conflict on 8080 or 5173
- **Cause:** Another background process is using port 8080 or 5173.
- **Fix:** Identify and stop the occupying PID:
  ```powershell
  Get-NetTCPConnection -LocalPort 8080 | Select-Object OwningProcess
  Stop-Process -Id <PID> -Force
  ```

### Issue C: Document Upload 413 "Payload Too Large"
- **Cause:** Reverse proxy or server limit exceeded.
- **Verification:** AI ScholarTwin is configured for 100 MB (`spring.servlet.multipart.max-file-size=100MB`). In Nginx, ensure `client_max_body_size 120M;` is active.

### Issue D: Tesseract OCR Not Found
- **Symptom:** OCR warnings in backend logs when uploading scanned PDFs.
- **Fix:** Install Tesseract on Windows (`winget install UB-Mannheim.TesseractOCR`) or set `OCR_ENABLED=false` in `.env` to fallback to native text-layer extraction.

---

## 13. Backup & Disaster Recovery

### Database Backup
```powershell
# Create timestamped SQL dump
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u scholartwin_user -pscholartwin_password ai_scholartwin > "backup_scholartwin_$timestamp.sql"
```

### Database Restore
```powershell
mysql -u scholartwin_user -pscholartwin_password ai_scholartwin < "backup_scholartwin_20261118_180000.sql"
```

### Document Storage Backup
```powershell
# Archive uploads directory
Compress-Archive -Path backend\uploads\documents -DestinationPath "backup_documents_$timestamp.zip"
```
