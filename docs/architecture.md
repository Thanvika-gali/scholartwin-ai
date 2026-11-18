# AI ScholarTwin — Full System Architecture Documentation

Comprehensive architecture overview of the **AI ScholarTwin** platform across all implemented milestones (M1–M10).

---

## 1. System Context & Topology

AI ScholarTwin operates as a high-performance, decoupled three-tier platform comprising an interactive React single-page client, a stateless Spring Boot 3 micro-modular backend, and an ACID-compliant MySQL 8 relational database, augmented by Google Gemini 2.5 Flash and local document extraction engines.

```
                              [ User Web Browser ]
                                      |
                         HTTP(S) Port 80 / 5173
                                      v
          +-------------------------------------------------------+
          |            EDGE / REVERSE PROXY LAYER (Nginx)         |
          | - Single-Origin Gateway (/api -> Backend:8080)        |
          | - Security Response Headers (HSTS, CSP, X-Frame DENY) |
          | - High-Capacity Upload Buffer (120 MB)                |
          | - Static Asset Caching & Gzip Compression             |
          +-------------------------------------------------------+
                                      |
                           Internal Bridge Network
                                      v
          +-------------------------------------------------------+
          |         APPLICATION LAYER (Spring Boot 3.4.3)         |
          |                                                       |
          |  [Security Filter Chain]                              |
          |    - Stateless JWT Validation (HMAC-SHA256)           |
          |    - BCrypt Password Encoder (Strength 12)            |
          |    - Cross-User Resource Isolation & Ownership Check  |
          |                                                       |
          |  [Business & Domain Services]                         |
          |    - Knowledge Hub & Document Extraction (PDFBox/POI) |
          |    - Tesseract OCR Scanned Document Ingestion        |
          |    - DSA Mastery Tracker & Spaced Repetition          |
          |    - Career Readiness & ATS Resume Parser             |
          |    - Adaptive Learning Roadmaps & Study Planner       |
          |    - AI Memory Center, Knowledge Graph & Analytics    |
          |    - Grounded AI Twin (Gemini 2.5 Flash + Fallback)   |
          +-------------------------------------------------------+
                     |                                |
         JDBC Port 3306                     HTTPS Port 443
                     v                                v
+-----------------------------------+   +---------------------------+
|    PERSISTENCE (MySQL 8.0.44)     |   |      EXTERNAL AI API      |
| - Dedicated DB: ai_scholartwin    |   | - Google Gemini 2.5 Flash |
| - 43 Relational Entity Tables     |   | - Grounded Academic Query |
| - Foreign Key Cascade Cleanup     |   | - Strict Privacy Shield   |
| - 16 Composite Query Indexes      |   +---------------------------+
+-----------------------------------+
```

---

## 2. Core Subsystem Architectures

### A. Document Ingestion & Chunking Pipeline (Milestone 4 & Upgrade)
```
[User File Upload (Up to 100MB)]
             |
             v
[File Validation Service] 
  -> Validates MIME headers & file signatures (PDF %PDF-, DOCX PK ZIP)
  -> Calculates SHA-256 hash for duplicate prevention
             |
             v
[Secure Disk Storage] 
  -> Stores file under random UUID outside web root
  -> Enforces path-traversal validation (rejects ../, /)
             |
             v
[Text Extraction Engine]
  -> Native PDF: Apache PDFBox 3.x
  -> Word Document: Apache POI 5.x (XWPFDocument)
  -> Scanned Fallback: Tesseract OCR 5.x (if enabled)
             |
             v
[Semantic Chunking Strategy]
  -> Chunks text into 1000-character segments with 150-character overlap
  -> Annotates each chunk with exact startPage and endPage
             |
             v
[Database Persistence]
  -> Persists Document and DocumentChunk entities with cascade delete
```

### B. Spaced Repetition Engine (Milestones 6 & 8)
- Based on the SuperMemo-2 / Ebbinghaus forgetting curve algorithm.
- Dynamically calculates revision intervals:
  $$\text{Interval}_{n} = \text{Interval}_{n-1} \times \text{EaseFactor}$$
- Integrates DSA problem review schedules with the student's daily study calendar.
- Enforces daily study hour caps to prevent cognitive overload.

### C. Career Readiness & Resume Extraction Pipeline (Milestone 7)
- Ingests student resumes in PDF/DOCX format (up to 20MB).
- Extracts sections: Contact Information, Technical Skills, Education, Professional Experience, Projects.
- Calculates an objective ATS Score (0–100) based on standard industry recruiting guidelines.
- Maps extracted competencies against target industry roles (Software Engineer, Backend Developer, Data Scientist, Cloud Architect).
- Generates targeted action plans with prioritized learning tasks to close identified gaps.

### D. AI Memory & Grounded Reasoning Pipeline (Milestones 8, 9 & 10)
- **Automatic Memory Extraction:** Activity signals from DSA logs, quiz results, and resume analyses are distilled into concise memory items.
- **Privacy & Security Redaction:** Regex-based sanitization automatically redacts API keys, credentials, and potential prompt injection payloads (`[REDACTED_SECRET]`, `[FILTERED_INJECTION]`).
- **Student Privacy Shield:** Students have full CRUD control over their memories; memories can be edited, pinned, or deleted at any time.
- **4-Tier Grounding Protocol:**
  1. `GEMINI_RESPONSE`: Live synthesis via Google Gemini 2.5 Flash enriched with student memories and knowledge citations.
  2. `PROVIDER_UNAVAILABLE`: Deterministic fallback via the local ScholarTwin Academic Reasoning Engine when Gemini is unconfigured or offline.
  3. `INSUFFICIENT_CONTEXT`: Transparent notification when no verified student data exists, preventing fabricated assumptions.
  4. `KNOWLEDGE_GROUNDED`: Direct citation of student-uploaded notes and textbooks.

---

## 3. Database Schema Overview (43 Tables)

```
[users]
  |--< [knowledge_items] (Notes, Links)
  |--< [documents] (PDFs, DOCX)
  |      \--< [document_chunks]
  |--< [knowledge_folders]
  |--< [knowledge_collections]
  |--< [dsa_problems]
  |      \--< [dsa_attempts]
  |      \--< [dsa_revisions]
  |--< [resumes]
  |      \--< [resume_analyses]
  |--< [career_skills]
  |--< [career_actions]
  |--< [learning_profiles]
  |--< [learning_roadmaps]
  |      \--< [roadmap_phases]
  |             \--< [learning_tasks]
  |--< [study_sessions]
  |--< [planner_tasks]
  |--< [spaced_revision_items]
  |      \--< [revision_attempts]
  |--< [learning_assessments]
  |      \--< [assessment_questions]
  |      \--< [assessment_attempts]
  |--< [user_memories]
  |--< [memory_preferences]
  |--< [ai_insights]
  |      \--< [ai_insight_evidences]
  |--< [ai_recommendations]
  |--< [ai_knowledge_nodes]
  |      \--< [ai_knowledge_relationships]
  \--< [ai_interaction_logs]
```

---

## 4. Security Hardening Specifications

1. **Authentication:** Stateless Bearer JWT with 256-bit HMAC secret and 24-hour expiration window.
2. **Password Storage:** BCrypt with 12 hashing rounds.
3. **Data Isolation:** All database repositories enforce user tenancy via `@Param("user") User user` or explicit `findByUser` lookups.
4. **File Defense:** UUID-based storage names, storage outside the public web root, MIME and magic byte validation, and path-traversal rejection.
5. **Prompt Injection Mitigation:** Sanitization of prompt inputs against common adversarial prefixes (`ignore previous instructions`, `dan mode`, `system prompt`).
6. **HTTP Headers:** Standard security headers (`X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `X-XSS-Protection`, `Strict-Transport-Security`).
