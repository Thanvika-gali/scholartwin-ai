# AI ScholarTwin — Architecture Overview

## 1. System Vision
AI ScholarTwin is an intelligent, personalized learning assistant designed to organize student knowledge, synthesize academic resources, track coding skills/DSA progress, evaluate career readiness, and generate adaptive learning journeys.

---

## 2. Milestone 1 Architecture

Milestone 1 establishes a decoupled, full-stack foundation ready for modular feature growth.

```
+-------------------------------------------------------------+
|                      Client Browser                         |
+-------------------------------------------------------------+
                               |
                               | (HTTP/JSON on :5173)
                               v
+-------------------------------------------------------------+
|               Frontend (React + Vite + Tailwind)            |
| - Vite Dev Server with Reverse Proxy on /api                |
| - Responsive UI components & state hooks                    |
+-------------------------------------------------------------+
                               |
                               | (Proxied to :8080)
                               v
+-------------------------------------------------------------+
|               Backend (Spring Boot 3 + Java 17)             |
| - Spring Web (REST Controllers & API contracts)             |
| - Spring Boot Actuator (Health & readiness probes)          |
| - Spring Data JPA & Hibernate (Object-Relational Mapping)   |
+-------------------------------------------------------------+
                               |
                               | (JDBC on :3306)
                               v
+-------------------------------------------------------------+
|                      MySQL 8.x Database                     |
| - Dedicated database: ai_scholartwin                        |
| - Non-root dedicated application user credentials           |
+-------------------------------------------------------------+
```

---

## 3. Communication Patterns

### Development Communication
- During local development, the React client communicates through the Vite development server (`http://localhost:5173`).
- All requests prefixed with `/api` are transparently proxied to the Spring Boot backend (`http://localhost:8080`).
- This avoids cross-origin (CORS) friction during development while simulating a production-like single-domain API topology.

### Response Standardization
All REST APIs adhere to a consistent JSON response envelope:
```json
{
  "status": "UP",
  "service": "AI ScholarTwin Backend",
  "timestamp": "2026-11-01T17:45:00.000Z",
  "version": "1.0.0"
}
```

---

## 4. Module & Directory Layout
```
AI-ScholarTwin/
├── frontend/           # React client application (Vite, Tailwind CSS)
├── backend/            # Spring Boot REST API service (Maven, Java 17)
├── database/           # Schemas, documentation, and migration scripts
└── docs/               # System architecture and engineering guidelines
```
