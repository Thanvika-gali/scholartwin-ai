# Architecture Decision Records (ADR)

## ADR 1: Adoption of Java 17 LTS Baseline
- **Context:** The system runtime environment has Eclipse Adoptium OpenJDK 17 installed and verified, alongside Oracle JDK 25, while Java 21 was not present.
- **Decision:** Target Java 17 (`<java.version>17</java.version>`) in Maven compiler configuration with Spring Boot 3.4.3.
- **Rationale:** Spring Boot 3 officially requires Java 17 as its foundation LTS baseline. Java 17 provides language features (records, sealed classes, pattern matching) and ensures long-term operational stability without requiring unverified system runtime modifications.

---

## ADR 2: Decoupled Vite Reverse Proxy for Development
- **Context:** During development, the frontend runs on port 5173 and backend on port 8080. Calling the backend directly requires CORS configuration.
- **Decision:** Configure Vite's built-in development proxy (`/api` -> `http://localhost:8080`).
- **Rationale:** Prevents CORS complexity during local development, mimics single-origin production deployments (where a reverse proxy like NGINX or Cloudflare routes `/api` to backend), and allows configurable target override via `VITE_BACKEND_URL`.

---

## ADR 3: Pure Tailwind CSS v4 Vite Setup
- **Context:** Mixing Tailwind CSS v3 (e.g. `postcss.config.js`, `tailwind.config.js`) with v4 causes syntax collision, sluggish builds, and dependency conflicts.
- **Decision:** Use official Tailwind CSS v4 with `@tailwindcss/vite` plugin and modern `@import "tailwindcss";` in `index.css`.
- **Rationale:** Eliminates redundant configuration files, provides sub-millisecond hot-module-reloading (HMR), and adheres strictly to current standards.

---

## ADR 4: Externalized Database Credentials
- **Context:** Hardcoding database credentials in properties files creates security vulnerabilities.
- **Decision:** Read `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` from environment variables, backed by sensible local defaults and documented in `.env.example`.
- **Rationale:** Ensures clean separation of configuration and secrets from the codebase, adhering to 12-Factor App methodology and preventing accidental Git leakage.
