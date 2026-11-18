# AI ScholarTwin

**AI ScholarTwin** is an intelligent, personalized academic knowledge management, DSA mastery, career readiness, and adaptive learning platform. It serves as an engineering student's autonomous "Academic Digital Twin" — organizing research materials, extracting textbook knowledge, guiding coding preparation with spaced repetition, matching resumes to target technical roles, and synthesizing insights with Google Gemini 2.5 Flash and a deterministic offline reasoning engine.

> **Status:** Production-Ready & Milestone 10 Complete — Full Test Suite (170 Backend Tests, 80 Frontend Tests Passing), Security Hardened, Dockerized, and Demonstration-Ready.

---

## 1. System Features Across Milestones 1–10

1. **Enterprise Foundation & Security (M1 & M2):**
   - Stateless JWT authentication (HMAC-SHA256, 256-bit secret) with BCrypt (strength 12) password hashing.
   - Strict cross-user isolation and ownership checks on every database entity.
   - Comprehensive security response headers: HSTS, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`.
2. **Dashboard & Smart Knowledge Hub 2.0 (M3 & M4 Upgrade):**
   - Ingestion of large academic documents up to **100 MB** (PDF, DOCX) and resumes up to **20 MB**.
   - Text extraction via Apache PDFBox 3.x and Apache POI 5.x, with optional Tesseract OCR for scanned papers.
   - Semantic chunking (1000 characters with 150-character overlap) with start/end page metadata.
   - SHA-256 duplicate detection, hierarchical folders, tagging, and cross-resource linking.
3. **DSA & Coding Mastery Tracker (M6):**
   - Multi-platform problem logging (LeetCode, Codeforces, HackerRank, CodeChef, GeeksforGeeks, NeetCode).
   - Attempt tracking with code snippets, time taken, difficulty breakdown, and solve rate analytics.
   - Automated Ebbinghaus / SuperMemo-2 spaced revision intervals synchronized with the learning calendar.
4. **Career & Resume Intelligence (M7):**
   - Automated PDF/DOCX resume parsing: technical skills, work experience, education, and project extraction.
   - Real-time ATS scoring (0–100) with detailed impact, formatting, and keyword suggestions.
   - Target role benchmarking (Software Engineer, Backend Developer, Data Scientist, Cloud Architect) with skill gap diagnostics and prioritized action plans.
5. **Personalized Learning & Revision Engine (M8):**
   - Adaptive student profiles (target role, experience level, learning style, daily study goal).
   - Milestone-driven learning roadmaps broken down into ordered phases and granular study tasks.
   - Daily study planner with conflict prevention and daily availability caps.
   - Topic assessments with real-time scoring, answer explanations, and mastery tracking.
6. **AI Insights, Memory & Analytics (M9):**
   - User memory extraction with privacy filtering and regex-based secret redaction (`[REDACTED_SECRET]`).
   - Interactive SVG/Canvas Knowledge Graph mapping mastered competencies and curriculum relationships.
   - Transparent, uninflated student analytics based on actual database activity.
7. **Quality Gate, Hardening & Deployment (M10):**
   - 100% automated test pass rate across 170 backend tests and 80 frontend tests.
   - Production Dockerfile and Docker Compose architecture with persistent volumes and health checks.
   - Comprehensive documentation: `DEPLOYMENT.md`, `DEMONSTRATION_GUIDE.md`, and `milestone-10-final-project-report.md`.

---

## 2. Technology Stack

- **Frontend:** React 19, Vite 8.3.5, Tailwind CSS v4, Lucide React, Vitest
- **Backend:** Java 17 LTS, Spring Boot 3.4.3, Spring Data JPA, Spring Security 6.x
- **Database:** MySQL 8.0.44 Community Server (`ai_scholartwin`)
- **Document Processing:** Apache PDFBox 3.0.3, Apache POI 5.3.0, Tesseract OCR 5.x
- **AI Integration:** Google Gemini 2.5 Flash + ScholarTwin Academic Reasoning Engine
- **Containerization:** Docker, Docker Compose, Multi-stage builds, Nginx Alpine

---

## 3. Quick Start Guide

### Prerequisites
- JDK 17
- Node.js v20+ & npm
- MySQL 8.x running on port 3306

### 1. Database Initialization
```powershell
mysql -u root -p < database/init.sql
```

### 2. Environment Setup
```powershell
Copy-Item .env.example .env
# Edit .env to add your GEMINI_API_KEY if desired
```

### 3. Start Backend (Port 8080)
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### 4. Start Frontend (Port 5173)
```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

---

## 4. Docker Deployment

To launch the full containerized stack (MySQL + Backend + Frontend/Nginx):

```powershell
docker compose up -d --build
```
Access the application at `http://localhost`.

---

## 5. Automated Verification & Testing

### Backend Test Suite (170 Tests)
```powershell
cd backend
.\mvnw.cmd test
```

### Frontend Test Suite (80 Tests)
```powershell
cd frontend
npm test -- --run
```

### Frontend Production Build
```powershell
cd frontend
npm run build
```

---

## 6. Project Documentation

- **Deployment Guide:** [DEPLOYMENT.md](file:///E:/projects/scholartwin-ai/DEPLOYMENT.md)
- **Demonstration Guide:** [DEMONSTRATION_GUIDE.md](file:///E:/projects/scholartwin-ai/DEMONSTRATION_GUIDE.md)
- **Architecture Overview:** [docs/architecture.md](file:///E:/projects/scholartwin-ai/docs/architecture.md)
- **Final Milestone 10 Report:** [milestone-10-final-project-report.md](file:///E:/projects/scholartwin-ai/milestone-10-final-project-report.md)
