# AI ScholarTwin — Milestones 8 & 9 Database & Datasource Audit

**Audit Date:** November 18, 2026  
**Auditor:** Antigravity Full-Stack AI Engineer  
**Workspace:** `E:\projects\scholartwin-ai`  
**Application Environment:** Java 17, Spring Boot 3.4.3, MySQL 8.0.44, React 19  

---

## 1. Executive Datasource Confirmation

| Parameter | Configured Value | Live Verification Status |
| :--- | :--- | :--- |
| **Active RDBMS** | MySQL 8.0.44 on port `3306` | **Connected & Active** |
| **Active Database** | `ai_scholartwin` | **Confirmed in application.properties & live runtime** |
| **Active JDBC URL** | `jdbc:mysql://localhost:3306/ai_scholartwin?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | **Verified working via HikariCP** |
| **Application DB User** | `scholartwin_user` / `scholartwin_password` | **Granted `ALL PRIVILEGES` on `ai_scholartwin.*`** |
| **Legacy Database** | `scholartwin` in MySQL | **Not used by application; access denied for app user** |
| **MongoDB Instance** | `mongodb://localhost:27017/scholartwin` | **Active on port 27017; NOT connected to Spring Boot** |
| **Vector Engine (Qdrant)** | Ports 6333 / 6334 | **Offline (TCP connection failed; not in codebase)** |

> [!IMPORTANT]
> **Active Database Policy:** In accordance with non-negotiable rules, the active database remains strictly `ai_scholartwin`. The active database was **not** changed to `scholartwin`. No tables, rows, or MongoDB documents were overwritten, truncated, or dropped.

---

## 2. Active MySQL (`ai_scholartwin`) Table Inventory

The active database `ai_scholartwin` contains **43 tables** managed by Spring Data JPA & Hibernate, supporting Milestones 1 through 9.

| # | Table Name | Purpose / Milestone | Records | User Ownership Column |
| :---: | :--- | :--- | :---: | :--- |
| 1 | `users` | Core User Accounts & Roles (M1/M2) | 76 | `id` (Primary Key) |
| 2 | `documents` | Uploaded academic documents & metadata (M4) | 14 | `user_id` -> `users(id)` |
| 3 | `document_chunks` | Chunked text representations (M4) | 23 | Relational join to `documents(id)` |
| 4 | `knowledge_folders` | Smart Knowledge Hub 2.0 folders (M3/M4) | 18 | `user_id` -> `users(id)` |
| 5 | `knowledge_items` | Vault notes, saved links, summaries (M3) | 12 | `user_id` -> `users(id)` |
| 6 | `knowledge_collections` | Academic learning collections (M3) | 4 | `user_id` -> `users(id)` |
| 7 | `knowledge_collection_items` | Collection join table (M3) | 4 | Relational join |
| 8 | `knowledge_organization_rules` | Auto-organization rule engine (M3) | 5 | `user_id` -> `users(id)` |
| 9 | `knowledge_organization_suggestions` | AI-suggested folder categorizations (M3) | 12 | `user_id` -> `users(id)` |
| 10 | `knowledge_resource_relations` | Cross-document semantic links (M3) | 2 | `user_id` -> `users(id)` |
| 11 | `knowledge_resource_tags` | Document tagging join table (M3) | 0 | Relational join |
| 12 | `knowledge_tags` | Tag definitions (M3) | 0 | `user_id` -> `users(id)` |
| 13 | `knowledge_view_history` | Document access history (M3) | 0 | `user_id` -> `users(id)` |
| 14 | `dsa_problems` | Problem repository (M6) | 11 | `user_id` -> `users(id)` |
| 15 | `dsa_problem_topics` | Secondary topics join table (M6) | 10 | Relational join |
| 16 | `dsa_attempts` | Problem solve attempt history (M6) | 10 | `user_id` -> `users(id)` |
| 17 | `dsa_revisions` | M6 Spaced repetition revision queue | 2 | `user_id` -> `users(id)` |
| 18 | `resumes` | Multi-version resume files & raw text (M7) | 1 | `user_id` -> `users(id)` |
| 19 | `resume_analyses` | 100-pt ATS diagnostic evaluations (M7) | 1 | `user_id` -> `users(id)` |
| 20 | `career_skills` | Verified technical & soft skills (M7) | 1 | `user_id` -> `users(id)` |
| 21 | `career_actions` | Action plan items (M7) | 1 | `user_id` -> `users(id)` |
| 22 | `job_description_analyses` | ATS job match diagnostics (M7) | 0 | `user_id` -> `users(id)` |
| 23 | `learning_profiles` | Student learning preferences & roles (M8) | 0 | `user_id` -> `users(id)` |
| 24 | `learning_goals` | Explicit study goals (M8) | 0 | Relational join to `learning_profiles` |
| 25 | `learning_roadmaps` | Adaptive curriculum roadmaps (M8) | 8 | `user_id` -> `users(id)` |
| 26 | `roadmap_phases` | Milestone phases in roadmaps (M8) | 24 | Relational join to `learning_roadmaps` |
| 27 | `learning_tasks` | Actionable learning roadmap tasks (M8) | 48 | `user_id` -> `users(id)` |
| 28 | `planner_tasks` | Daily planner tasks (M8) | 5 | `user_id` -> `users(id)` |
| 29 | `study_sessions` | Time-tracked study logs (M8) | 0 | `user_id` -> `users(id)` |
| 30 | `spaced_revision_items` | M8 Spaced repetition schedule (M8) | 4 | `user_id` -> `users(id)` |
| 31 | `revision_attempts` | Revision attempt log (M8) | 4 | `user_id` -> `users(id)` |
| 32 | `learning_assessments` | Quiz / assessment instances (M8) | 0 | `user_id` -> `users(id)` |
| 33 | `assessment_questions` | Pre-generated quiz question pool (M8) | 13 | Relational join |
| 34 | `assessment_attempts` | Student quiz submission logs (M8) | 0 | `user_id` -> `users(id)` |
| 35 | `learning_resource_associations` | Task-to-resource links (M8) | 2 | Relational join |
| 36 | `user_memories` | Long-term personalized memory (M9) | 9 | `user_id` -> `users(id)` |
| 37 | `memory_preferences` | Category privacy toggles (M9) | 0 | `user_id` -> `users(id)` |
| 38 | `ai_insights` | Diagnostic academic insights (M9) | 10 | `user_id` -> `users(id)` |
| 39 | `ai_insight_evidences` | Raw metric evidence records (M9) | 8 | Relational join to `ai_insights` |
| 40 | `ai_recommendations` | Prioritized recommendation queue (M9) | 5 | `user_id` -> `users(id)` |
| 41 | `ai_knowledge_nodes` | Topological knowledge graph nodes (M9) | 12 | `user_id` -> `users(id)` |
| 42 | `ai_knowledge_relationships` | Directed knowledge graph edges (M9) | 13 | `user_id` -> `users(id)` |
| 43 | `ai_interaction_logs` | AI Twin conversational telemetry (M9) | 0 | `user_id` -> `users(id)` |

---

## 3. MongoDB (`scholartwin`) Collection Inventory

Inspected via `mongosh` on `mongodb://localhost:27017/scholartwin`:

| Collection | Count | Legacy Java Class Header | Notes & Sample Fields |
| :--- | :---: | :--- | :--- |
| `uploaded_documents` | 11 | `com.scholartwin.documents.UploadedDocument` | Contains `userId` (values 1, 2), `filename`, `rawText`, `fileType`, `metadata`. |
| `knowledge_chunks` | 168 | `com.scholartwin.documents.KnowledgeChunk` | Text chunks with `chunkIndex`, `content`, `qdrantPointId`. |
| `chat_conversations` | 12 | `com.scholartwin.chat.ChatConversation` | Message arrays (`sender`, `content`, `timestamp`) with `userId` 1. |
| `users` | 0 | N/A | Empty collection. |

### Key Findings Regarding MongoDB:
1. **Application Usage:** The current Spring Boot application (`E:\projects\scholartwin-ai\backend`) does **NOT** depend on MongoDB. `pom.xml` contains zero MongoDB dependencies, and `application.properties` does not declare a MongoDB connection string.
2. **Data Origin:** The 11 documents, 168 chunks, and 12 chats originate from an earlier prototype (package prefix `com.scholartwin.*` vs current `com.aischolartwin.*`).
3. **User ID Disconnect:** The MongoDB records reference `userId: 1` and `userId: 2`. In the active MySQL database `ai_scholartwin`, active users have auto-increment IDs in the 300–800 range (e.g. `scholar.twin@university.edu` has ID 845).
4. **Current Document Pipeline:** Milestone 4 already stores all uploaded documents and extracted text chunks natively in MySQL tables `documents` and `document_chunks`.

---

## 4. Qdrant Vector Engine Status

- **Host Tested:** `localhost:6333` (REST HTTP) & `localhost:6334` (gRPC).
- **Result:** Both connection tests returned `TcpTestSucceeded: False`. Qdrant is **NOT running**.
- **Codebase Search:** Zero occurrences of `qdrant` in `backend/src` or `pom.xml`.
- **Retrieval Implementation in Current Application:**
  - AI Twin memory context assembly uses JPA-indexed queries (`UserMemoryRepository.searchMemories`, `findByUserAndStatusOrderByIsPinnedDescUpdatedAtDesc`).
  - Document context in chat uses keyword & title matching over `knowledge_items` and `documents`.
  - No vector database is currently running, and none is claimed as operational in the live system.

---

## 5. Safe Migration Strategy Recommendation (Optional Future Enhancement)

If the user wishes to migrate the 11 legacy MongoDB documents and 12 conversations into MySQL `ai_scholartwin`:

1. **Prerequisite:** Create a target user mapping (e.g., assign legacy `userId: 1` and `userId: 2` to an existing or designated MySQL user like `scholar.twin@university.edu`).
2. **Schema Translation:**
   - Map `uploaded_documents` -> MySQL `documents` table (`title`, `file_type`, `extracted_text`, `status = PROCESSED`).
   - Map `knowledge_chunks` -> MySQL `document_chunks` table (`chunk_index`, `chunk_text`, `document_id`).
   - Map `chat_conversations.messages` -> MySQL `ai_interaction_logs` (`query`, `response`, `grounding_mode = 'LEGACY_MIGRATION'`).
3. **Safety Guarantee:** Run as an idempotent migration script that checks for existing `filename` before inserting.
4. **Retention:** Keep MongoDB `scholartwin` intact; never drop legacy collections.
