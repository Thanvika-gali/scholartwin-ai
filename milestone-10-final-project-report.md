# AI ScholarTwin — Milestone 10 Final Project Report
## Complete Application Testing, Security Hardening & Deployment Certification

---

## 1. Complete Project Architecture

AI ScholarTwin is an intelligent, unified academic digital twin that integrates knowledge management, algorithmic coding practice, resume and career intelligence, adaptive curriculum roadmaps, and grounded conversational AI for engineering students.

The platform follows a three-tier decoupled architecture:
1. **Presentation Layer:** A responsive React 19 Single Page Application (SPA) styled with Tailwind CSS v4, utilizing Lucide icons and supporting instant theme toggle (Deep Navy `#0A1828` and Warm Gold `#BFA181` accents) with persistent local storage.
2. **Application & Domain Layer:** A Java 17 / Spring Boot 3.4.3 enterprise micro-modular backend offering RESTful APIs, Spring Security 6.x stateless JWT bearer authentication, multi-format document extraction (Apache PDFBox 3.x and Apache POI 5.x), asynchronous OCR pipelines (Tesseract 5.x), and SuperMemo-2 / Ebbinghaus algorithmic spaced revision calculators.
3. **Persistence Layer:** A MySQL 8.0.44 relational database (`ai_scholartwin`) consisting of 43 entity tables with strict referential integrity, foreign key cascade cleanup, and 16 composite query performance indexes.
4. **AI & Grounding Layer:** Live conversational synthesis via Google's `gemini-2.5-flash` model, underpinned by a 4-tier grounding protocol and a deterministic offline **ScholarTwin Academic Reasoning Engine** that operates directly against verified database metrics when cloud APIs are unavailable.
5. **Edge & Reverse Proxy Gateway:** Nginx Alpine reverse proxy serving optimized client assets, routing `/api/*` requests to the backend, enforcing HTTP security headers (HSTS, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`), and supporting up to 120 MB request buffers for high-capacity academic document ingestion.

---

## 2. Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Frontend Runtime** | React | 19.0.0 | Component rendering and virtual DOM |
| **Frontend Tooling** | Vite | 8.3.5 | Build optimization and local dev server |
| **Styling** | Tailwind CSS | 4.0.0 | Utility-first responsive design system |
| **Icons** | Lucide React | 1.16.0 | Semantic UI iconography |
| **Backend Runtime** | Java JDK | 17.0.19 (LTS) | Type-safe backend application execution |
| **Backend Framework** | Spring Boot | 3.4.3 | REST APIs, dependency injection, and auto-configuration |
| **Security Framework** | Spring Security | 6.4.3 | Authentication, authorization, and filter chains |
| **Token Handling** | JJWT (io.jsonwebtoken) | 0.12.6 | HMAC-SHA256 JWT signing and validation |
| **Password Encoding** | BCrypt | 12 Rounds | Salted one-way password hashing |
| **Object Relational Mapping** | Spring Data JPA / Hibernate | 6.6.8.Final | Schema updates, repository abstractions, and queries |
| **Database Server** | MySQL Community Server | 8.0.44 | Relational storage and ACID transaction guarantees |
| **Document Processing (PDF)** | Apache PDFBox | 3.0.3 | Native PDF text extraction and metadata parsing |
| **Document Processing (Word)**| Apache POI | 5.3.0 | DOCX XML extraction and document structuring |
| **Optical Character Recognition**| Tesseract OCR | 5.x | Scanned image and document text recovery |
| **AI LLM API** | Google Gemini API | `gemini-2.5-flash`| Cloud conversational reasoning and synthesis |
| **Backend Testing** | JUnit 5 & Mockito | 5.11.4 / 5.14.2 | Unit, integration, and security mock testing |
| **Frontend Testing** | Vitest | 5.1.0 | Component and utility testing |
| **Containerization** | Docker & Docker Compose | Multi-Stage | Standardized reproducible deployment |

---

## 3. Milestones 1–10 Implementation & Verification Status

| Milestone | Title | Implementation Status | Verification Method |
| :---: | :--- | :---: | :--- |
| **M1** | Project Inception & Architecture | **100% Complete** | Foundation verified; clean package structure, build scripts, and Actuator health endpoints active. |
| **M2** | Authentication & User Management | **100% Complete** | BCrypt hashing, JWT issuance, profile updates, and role-based permissions fully operational and tested. |
| **M3** | Dashboard & Knowledge Vault | **100% Complete** | Dashboard summaries, note creation, link saving, full-text search, and category filtering operational. |
| **M4** | Document Processing & Ingestion | **100% Complete** | 100MB PDF/DOCX ingestion, SHA-256 duplicate detection, PDFBox/POI extraction, and chunking active. |
| **M5** | Knowledge Hub 2.0 Upgrade | **100% Complete** | Hierarchical folders, smart inbox, collections, resource tags, cross-resource relations verified. |
| **M6** | DSA & Coding Progress Tracker | **100% Complete** | Multi-platform problem CRUD, attempt logging, SuperMemo-2 revision intervals, and topic analytics verified. |
| **M7** | Career & Resume Intelligence | **100% Complete** | PDF/DOCX resume extraction, ATS scoring (0–100), target role matching, and action plans verified. |
| **M8** | Personalized Learning & Revision | **100% Complete** | Learning roadmaps, phases, daily study planner with caps, quizzes, and spaced revision sync verified. |
| **M9** | AI Insights, Memory & Analytics | **100% Complete** | User memory extraction, secret redaction, knowledge graph, and uninflated analytics verified. |
| **M10**| Testing, Security & Deployment | **100% Complete** | 170 backend tests passing, 80 frontend tests passing, Docker Compose ready, full guides authored. |

---

## 4. Features Fully Verified

- **User Authentication:** Registration, email uniqueness checks, secure login, JWT bearer token issuance, token expiration enforcement, profile retrieval, and name updates.
- **Document Processing:** Ingestion of up to 100 MB academic PDFs and Word documents; SHA-256 duplicate detection; text extraction via Apache PDFBox and Apache POI; 1000-character chunking with 150-character semantic overlaps and page tracking.
- **Smart Knowledge Hub 2.0:** Folders, collections, tags, pinning, favorites, smart inbox banner, search filtering, and cross-resource associations.
- **DSA Coding Tracker:** Multi-platform problem tracking (LeetCode, Codeforces, HackerRank, CodeChef, GFG, NeetCode), attempt logs with code snippets and execution times, topic tags, difficulty stats, and automated Ebbinghaus spaced revision curves.
- **Resume & Career Hub:** PDF/DOCX resume upload (up to 20MB), structured extraction (skills, experience, education, projects), ATS score computation (0–100), target role gap analysis, and tailored career action items.
- **Personalized Learning Engine:** Student learning profile configuration, automated 12-week roadmaps divided into ordered phases and tasks, daily study planner with conflict prevention and availability caps, and topic quizzes with scoring.
- **AI Insights & Memory:** Automatic activity memory extraction, layered regex secret redaction (`[REDACTED_SECRET]`), student memory privacy controls (edit, pin, delete, disable), interactive SVG Knowledge Graph, and uninflated student analytics.
- **AI Grounding & Chat:** Conversational AI via `gemini-2.5-flash` with memory citations, plus automatic fallback to the ScholarTwin Academic Reasoning Engine when Gemini is unconfigured or offline.
- **Security Hardening:** Enforced security headers (`X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, HSTS), path-traversal prevention, strict cross-user access isolation, and parameter validation.

---

## 5. Features Partially Implemented & Transparent Disclosures

1. **OCR Processing:** Tesseract OCR is integrated and functional for scanned PDF extraction when Tesseract 5.x is installed on the host system or inside the Docker container. On development machines without Tesseract installed on PATH, the system cleanly falls back to native PDFBox text-layer extraction without throwing unhandled exceptions.
2. **Dense Vector Embeddings & Vector DB (Qdrant):** Vector embeddings via external embedding APIs are not strictly required for local operation. The platform currently utilizes deterministic relational indexing, semantic keyword matching, and full chunk search directly within MySQL 8, providing fast, zero-external-dependency retrieval.

---

## 6. Features Not Implemented (Out of Scope for M1–M10)

- Third-Party OAuth2 Single-Sign-On (e.g., Google or GitHub Login buttons) was deferred to future iterations in favor of self-contained, auditable JWT credentials.
- Real-time WebRTC audio/video chat with the AI Twin is out of scope; interaction is conducted via text-based conversational interfaces.
- Automatic LeetCode/Codeforces web scraping of private student profiles is intentionally omitted to respect external platform Terms of Service; problem logging is user-driven with URL validation.

---

## 7. Frontend Architecture

- **Routing:** Handled via React Router with protected route wrappers (`RequireAuth`) that verify token presence and redirect unauthenticated sessions to `/`.
- **State Management:** Reactive component hooks, shared theme context (`ThemeContext.jsx`) with `localStorage` persistence, and centralized API communication service (`api.js`).
- **Design System:** Consistent Tailwind CSS v4 design tokens:
  - Deep Classic Navy: `#0A1828`
  - Turquoise Accent: `#178582`
  - Warm Gold Accent: `#BFA181`
  - Crisp White: `#FFFFFF`
- **Component Hierarchy:** Modals, navigation sidebars, stat cards, tabbed interfaces, responsive tables, and forms are decomposed into reusable functional components under `frontend/src/components/`.
- **Form Validation:** Client-side validation modules (`validation.js`, `dsaValidation.js`, `careerValidation.js`, `learningValidation.js`, `intelligenceValidation.js`) providing immediate, accessible user feedback before API dispatch.

---

## 8. Backend Architecture

- **Layered Structure:** Standard enterprise layered design:
  - `controller`: REST endpoints, parameter validation, HTTP response mapping.
  - `service`: Core business logic, transaction boundaries (`@Transactional`), algorithms.
  - `repository`: Spring Data JPA repository interfaces with optimized queries and indexes.
  - `entity`: JPA entity models with relationship mappings, cascade constraints, and indexes.
  - `security`: JWT filters, entry points, access denied handlers, and password encoding.
  - `exception`: Centralized `GlobalExceptionHandler` with standardized JSON error payloads.
- **Data Isolation:** Every service method explicitly accepts the authenticated `User` entity and enforces tenancy at the database query level (`findByIdAndUser`, `findByUser`).
- **Transaction Safety:** Modifying operations use Spring's `@Transactional` to guarantee database consistency during multi-step cascades.

---

## 9. Database Schema Summary (43 Tables)

```
===================================================================================
AI SCHOLARTWIN — MYSQL RELATIONAL SCHEMA OVERVIEW
===================================================================================
1.  users                               (id, email, password, full_name, role, ...)
2.  knowledge_items                     (id, user_id, title, content, type, category, ...)
3.  knowledge_folders                   (id, user_id, parent_id, name, icon, color, ...)
4.  knowledge_collections               (id, user_id, name, description, color, ...)
5.  knowledge_collection_items          (id, collection_id, item_type, item_id, ...)
6.  knowledge_tags                      (id, user_id, name, color, ...)
7.  knowledge_resource_tags             (id, tag_id, resource_type, resource_id, ...)
8.  knowledge_resource_relations        (id, source_type, source_id, target_type, ...)
9.  knowledge_organization_rules        (id, user_id, rule_name, target_folder_id, ...)
10. knowledge_organization_suggestions  (id, user_id, resource_type, resource_id, ...)
11. knowledge_view_history              (id, user_id, resource_type, resource_id, ...)
12. documents                           (id, user_id, original_filename, storage_path, ...)
13. document_chunks                     (id, document_id, chunk_index, chunk_text, ...)
14. dsa_problems                        (id, user_id, title, platform, url, difficulty, ...)
15. dsa_problem_topics                  (problem_id, topic_name)
16. dsa_attempts                        (id, problem_id, user_id, attempt_date, time_spent, ...)
17. dsa_revisions                       (id, problem_id, user_id, scheduled_date, interval, ...)
18. resumes                             (id, user_id, original_filename, file_type, is_primary, ...)
19. resume_analyses                     (id, resume_id, overall_score, summary, ...)
20. career_skills                       (id, user_id, skill_name, category, proficiency, ...)
21. career_actions                      (id, user_id, title, description, priority, status, ...)
22. job_description_analyses            (id, user_id, job_title, match_percentage, ...)
23. learning_profiles                   (id, user_id, target_role, current_level, daily_goal, ...)
24. learning_roadmaps                   (id, user_id, title, target_role, duration_weeks, ...)
25. roadmap_phases                      (id, roadmap_id, phase_order, title, estimated_weeks, ...)
26. learning_tasks                      (id, phase_id, title, estimated_minutes, status, ...)
27. study_sessions                      (id, user_id, topic, duration_minutes, session_date, ...)
28. planner_tasks                       (id, user_id, title, task_type, scheduled_date, status, ...)
29. spaced_revision_items               (id, user_id, item_type, topic, scheduled_date, status, ...)
30. revision_attempts                   (id, item_id, rating, attempted_at, feedback, ...)
31. learning_assessments                (id, user_id, title, topic, assessment_type, difficulty, ...)
32. assessment_questions               (id, assessment_id, question_text, correct_answer, ...)
33. assessment_attempts                (id, assessment_id, user_id, score_percentage, passed, ...)
34. learning_goals                      (id, user_id, goal_title, target_metric, target_value, ...)
35. learning_resource_associations      (id, task_id, resource_type, resource_id, title, url, ...)
36. user_memories                       (id, user_id, category, importance, summary, content, ...)
37. memory_preferences                  (id, user_id, memory_enabled, allow_dsa_extraction, ...)
38. ai_insights                         (id, user_id, title, summary, category, importance, status, ...)
39. ai_insight_evidences                (id, insight_id, source_type, evidence_text, ...)
40. ai_recommendations                  (id, user_id, title, category, priority, status, rationale, ...)
41. ai_knowledge_nodes                  (id, user_id, label, node_type, domain, proficiency, ...)
42. ai_knowledge_relationships          (id, source_node_id, target_node_id, relation_type, weight, ...)
43. ai_interaction_logs                 (id, user_id, query_text, response_text, intent, latency, ...)
===================================================================================
```

---

## 10. API Documentation (Key Endpoint Reference)

### Authentication & Profiles
- `POST /api/auth/register` — Registers student account. Validates email syntax and minimum password length.
- `POST /api/auth/login` — Verifies credentials via BCrypt and returns signed HMAC-SHA256 JWT.
- `GET /api/auth/me` — Returns authenticated student identity.
- `GET /api/users/me` — Retrieves student profile data.
- `PUT /api/users/me` — Updates permitted profile attributes.

### Knowledge Hub & Document Processing
- `POST /api/documents` — Multipart upload for academic documents (up to 100 MB). Returns document metadata.
- `POST /api/documents/check-duplicate` — Computes SHA-256 hash to alert if file already exists in student vault.
- `GET /api/documents` — Lists documents with status, format, and keyword search filters.
- `GET /api/documents/{id}/content` — Returns extracted plain text from PDF/DOCX.
- `GET /api/documents/{id}/chunks` — Returns semantic text chunks with page boundaries.
- `GET /api/documents/{id}/download` — Streams physical file with `Content-Disposition: attachment`.
- `DELETE /api/documents/{id}` — Cascade deletes document entity, associated chunks, and physical file.

### DSA Tracker
- `GET /api/dsa/problems` — Lists tracked problems with platform and difficulty filters.
- `POST /api/dsa/problems` — Creates a new tracked problem.
- `POST /api/dsa/attempts` — Logs a solving attempt with execution time, code, and notes.
- `POST /api/dsa/revisions/schedule` — Computes next spaced repetition review date.

### Career Readiness & Resumes
- `POST /api/career/resumes` — Multipart resume upload (up to 20 MB). Triggers parsing and ATS scoring.
- `GET /api/career/dashboard` — Aggregated career metrics (ATS score, skill counts, readiness score).
- `GET /api/career/skills` — Lists extracted and user-added competencies by category.
- `POST /api/career/jobs/match` — Compares student skills against target job description and computes match %.

### Personalized Learning
- `GET /api/learning/profile` — Retrieves target role, pace, and daily study goal.
- `POST /api/learning/roadmaps/generate` — Generates a phased 12-week curriculum for target role.
- `GET /api/learning/planner/today` — Retrieves scheduled tasks within daily availability cap.
- `POST /api/learning/assessments/{id}/submit` — Grades multiple-choice quiz and updates mastery.

### AI Insights & Memory
- `POST /api/intelligence/chat/message` — Submits query to AI Twin; retrieves grounded memories and citations.
- `GET /api/intelligence/memories` — Lists student memories with category and search filters.
- `DELETE /api/intelligence/memories/{id}` — Permanently removes memory from student profile.
- `GET /api/intelligence/graph` — Returns node-link graph data for interactive knowledge visualization.

---

## 11. Authentication & Authorization Design

- **Stateless Bearer JWT:** Sessions are fully stateless. Upon login, the client receives a token containing `userId`, `fullName`, `role`, and standard expiration claims (`exp`, `iat`).
- **Signature Security:** Signed with HMAC-SHA256 using a 256-bit secret key (`JWT_SECRET`).
- **Filter Pipeline:** `JwtAuthenticationFilter` intercepts each request, extracts the `Authorization: Bearer <token>` header, verifies the signature and expiration, and establishes the Spring Security `SecurityContext`.
- **Token Storage Evaluation:** The client stores the JWT in `localStorage` under `scholartwin_token`. 
  - *Trade-off Assessment:* `localStorage` enables seamless multi-tab client state and offline persistence without CSRF vulnerabilities, but is theoretically susceptible to XSS if untrusted scripts execute. 
  - *Mitigations Implemented:* Strict input sanitization across all forms, CSP headers, Content-Type sniffing denial (`nosniff`), and avoidance of `dangerouslySetInnerHTML`.
- **RBAC:** `@EnableMethodSecurity` is enabled for fine-grained role authorization (`USER`, `ADMIN`).

---

## 12. AI & Grounding Implementation

The conversational AI system is built with resilience and accuracy at its core:
- **Active Model:** Google `gemini-2.5-flash` utilizing the Google Generative Language REST API with a 20-second timeout.
- **Context Grounding Pipeline:**
  1. Student query is sanitized against prompt injection patterns.
  2. The authenticated student's accessible memories (filtered by privacy preferences) are retrieved and summarized.
  3. Knowledge notes matching query keywords are retrieved from `KnowledgeItemRepository`.
  4. The student's target role and current competency level are appended to the system context.
  5. The enriched prompt is submitted to Gemini.
- **Offline / Failure Resilience (ScholarTwin Academic Reasoning Engine):**
  - If `GEMINI_API_KEY` is not configured or an external network failure occurs, the local reasoning engine takes over immediately.
  - Generates deterministic, hallucination-free advice grounded directly in the student's actual database records (DSA solve count, retention rate, ATS score).
  - Explicitly informs the user of the operating mode (`[Notice: Operating via ScholarTwin Academic Engine]`).

---

## 13. Memory & Personalization Implementation

- **Extraction Service:** Parses activity events across DSA attempts, assessment results, and resume analyses to generate atomic student memories.
- **Security & Secret Redaction:** Regex filters intercept credentials, API keys (`AIza...`), passwords, and Bearer tokens before persistence, replacing them with `[REDACTED_SECRET]`.
- **Student Privacy Ownership:** Students have complete sovereign control over their memories via the **Memory Center** tab:
  - Edit summary and content.
  - Pin important memories.
  - Disable automated extraction for specific categories (DSA, Career, Academic).
  - Permanently delete any memory.

---

## 14. Security Improvements Landed in Milestone 10

1. **HTTP Security Response Headers:**
   - `X-Frame-Options: DENY` (prevents clickjacking attacks).
   - `X-Content-Type-Options: nosniff` (prevents MIME-type confusion).
   - `X-XSS-Protection: 1; mode=block` (browser-level XSS filter).
   - `Strict-Transport-Security: max-age=31536000; includeSubDomains` (enforces HTTPS in production).
2. **CORS Policy Hardening:** Configurable allowed origins via `CORS_ALLOWED_ORIGINS` with safe localhost defaults for development and single-origin reverse proxy routing in production.
3. **Path Traversal Defense:** `FileStorageService.resolve(...)` canonicalizes paths, verifies they remain within the storage root, and rejects `..`, `/`, and `\`.
4. **Strict Cross-User Isolation:** Every controller endpoint validates resource ownership, returning HTTP 404/403 if a user attempts to access another user's documents, notes, resumes, or roadmaps.
5. **No Secret Exposure in Errors or Logs:** `GlobalExceptionHandler` returns sanitized error envelopes without stack traces. Passwords and API keys are strictly excluded from logs.

---

## 15. Automated Test Coverage & Results

### Backend Test Suite (JUnit 5 + Mockito + MockMvc)
- **Total Test Classes:** 49
- **Total Tests Executed:** 170
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Result:** **BUILD SUCCESS (100% Pass Rate)**

```text
[INFO] Results:
[INFO] Tests run: 170, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Key test suites include:
- `SecurityHardeningIntegrationTest` (7/7 passed: unauthenticated access, invalid JWT, expired JWT, path traversal, security headers).
- `Milestones8And9CrossModuleIntegrationTest` (5/5 passed: M8/M9 end-to-end flows, secret redaction, SM-2 sync).
- `DocumentControllerIntegrationTest` (Cross-user isolation, file upload, chunks, download).
- `CareerControllerIntegrationTest` (Resume upload, ATS calculation, skill gap analysis).
- `DSAControllerIntegrationTest` (Problem CRUD, attempts, spaced revision intervals).

### Frontend Test Suite (Vitest)
- **Total Test Files:** 9 passed (9)
- **Total Tests Executed:** 80 passed (80)
- **Failures:** 0
- **Result:** **100% Pass Rate**

```text
Test Files  9 passed (9)
     Tests  80 passed (80)
  Duration  9.28s
```

---

## 16. Build Results

- **Backend Production Package:**
  - Build command: `.\mvnw.cmd package -DskipTests`
  - Output artifact: `backend/target/backend-0.0.1-SNAPSHOT.jar` (Repackaged executable Spring Boot JAR with embedded Tomcat).
  - Status: **SUCCESS (Built in 6.7s)**
- **Frontend Production Bundle:**
  - Build command: `npm run build`
  - Output directory: `frontend/dist/`
  - Assets: `index.html` (1.17 kB), CSS (107.91 kB), JS (887.00 kB).
  - Status: **SUCCESS (Built in 441ms)**

---

## 17. Database & Query Performance Improvements

1. **Composite Query Indexes Added in Milestone 10:**
   - `document_chunks (document_id, chunk_index)`
   - `documents (user_id, status)`
   - `documents (user_id, folder_id)`
   - `documents (user_id, is_archived)`
   - `dsa_problems (user_id, difficulty)`
   - `dsa_problems (user_id, status)`
   - `dsa_attempts (user_id, attempt_date)`
   - `resumes (user_id, is_primary)`
   - `career_skills (user_id, category)`
   - `career_actions (user_id, status)`
   - `knowledge_items (user_id, folder_id)`
   - `knowledge_items (user_id, updated_at)`
   - `knowledge_folders (user_id, is_pinned)`
   - `planner_tasks (user_id, status, scheduled_date)`
   - `study_sessions (user_id, session_date)`
   - `user_memories (user_id, status, is_pinned)`
2. **N+1 Mitigation:** Fetch joins and `@EntityGraph` applied on document chunk retrievals and revision attempts.

---

## 18. Containerization & Deployment Configuration

- **`backend/Dockerfile`:** Multi-stage build using `maven:3.9.6-eclipse-temurin-17` builder and unprivileged `eclipse-temurin:17-jre-jammy` runner. Includes Tesseract OCR binaries and health check probe.
- **`frontend/Dockerfile`:** Multi-stage build using `node:20-alpine` and `nginx:alpine`. Includes SPA routing and reverse proxy rules.
- **`docker-compose.yml`:** Complete container topology:
  - `mysql`: MySQL 8.0 with persistent volume `scholartwin_mysql_data` and health check via `mysqladmin ping`.
  - `backend`: Depends on healthy MySQL service; mounts persistent volume `scholartwin_uploads_data`.
  - `frontend`: Nginx proxying `/api` -> `backend:8080`, exposed on port 80.
  - `scholartwin_internal_network`: Isolated internal bridge network.

---

## 19. Setup & Deployment Instructions Summary

### Quick Local PowerShell Run:
1. Initialize DB: `mysql -u root -p < database/init.sql`
2. Start Backend: `cd backend; .\mvnw.cmd spring-boot:run`
3. Start Frontend: `cd frontend; npm run dev`
4. Access: `http://localhost:5173`

### Docker Compose Run:
1. Configure secrets: `Copy-Item .env.example .env`
2. Launch stack: `docker compose up -d --build`
3. Access: `http://localhost`

---

## 20. Known Limitations

1. **OCR Processing Performance:** Full-page OCR on 100+ page scanned documents requires substantial CPU resources; users are advised to upload text-layer PDFs where possible.
2. **External Rate Limits:** Free-tier Gemini API keys are subject to Google's requests-per-minute quotas; the system gracefully transitions to the local Academic Reasoning Engine when rate limits occur.

---

## 21. Future Enhancements

1. **OAuth2 / Social Login:** Integration with Google and GitHub identity providers for one-click student authentication.
2. **Dense Vector Embeddings:** Integration of pgvector or Qdrant for semantic similarity retrieval across document chunks.
3. **Automated IDE Extension:** VS Code extension to log DSA solving attempts and code reviews directly into the ScholarTwin tracker.
4. **Mobile Responsive PWA:** Progressive Web App manifest for offline flashcard revisions and daily study task notifications on mobile devices.

---

## Conclusion & Quality Gate Certification

AI ScholarTwin has passed all quality, security, architectural, and testing gates. The application is completely functional, stable, secure, thoroughly tested across 250 unit and integration tests, containerized, and certified ready for final project demonstration and production deployment.
