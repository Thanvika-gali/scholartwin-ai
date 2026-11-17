# AI ScholarTwin

**AI ScholarTwin** is an AI-powered personalized academic and engineering learning assistant. It organizes student knowledge, understands course and research materials, assists in problem-solving, tracks algorithmic (DSA) coding progress, analyzes resume and career readiness, and devises personalized learning curricula.

> **Status:** Production-Ready — Milestones 1 through 7 Complete, Critical Bug Fixes & Product Improvements, and Smart Knowledge Hub 2.0 Implemented & Verified

---

## Technology Stack

- **Frontend:** React 19, Vite, Tailwind CSS v4, React Router 7
- **Backend:** Java 17, Spring Boot 3.4.3, Spring Security 6.x
- **Document Processing:** Apache PDFBox 3.0.3, Apache POI 5.3.0 (`poi-ooxml`)
- **Authentication:** Stateless JWT (HMAC-SHA256 via JJWT), BCrypt (12 rounds)
- **ORM / Persistence:** Spring Data JPA, Hibernate (ddl-auto=update)
- **Database:** MySQL 8.x (`ai_scholartwin`)
- **Build & Test Tools:** Maven Wrapper (`mvnw`), npm, Vitest

---

## Core API Endpoints

### Health & Authentication
| Endpoint | Method | Security | Description |
| :--- | :--- | :--- | :--- |
| `/api/health` | `GET` | Public | Service health & MySQL connection status |
| `/api/auth/register` | `POST` | Public | Register new student account (`fullName`, `email`, `password`) |
| `/api/auth/login` | `POST` | Public | Authenticate user credentials and return signed JWT |
| `/api/auth/me` | `GET` | Authenticated | Retrieve currently authenticated user identity |
| `/api/auth/logout` | `POST` | Public/Auth | Client-side session termination |
| `/api/users/me` | `GET` | Authenticated | Fetch authenticated user profile |
| `/api/users/me` | `PUT` | Authenticated | Update permitted profile fields (`fullName`) |

### Dashboard & Knowledge Hub (Milestones 3 & 4)
| Endpoint | Method | Security | Description |
| :--- | :--- | :--- | :--- |
| `/api/dashboard/summary` | `GET` | Authenticated | Aggregated metrics (`totalItems`, `totalDocuments`, `totalNotes`, `totalLinks`) and recent items |
| `/api/knowledge-items` | `GET` | Authenticated | List items with optional filters (`type=NOTE\|LINK`, `search=query`, `category=cat`) |
| `/api/knowledge-items` | `POST` | Authenticated | Create a new Note (`type=NOTE`) or Link (`type=LINK`) |
| `/api/knowledge-items/{id}` | `GET` | Authenticated | Retrieve a specific item by ID (strictly user-isolated) |
| `/api/knowledge-items/{id}` | `PUT` | Authenticated | Update an existing item (strictly user-isolated) |
| `/api/knowledge-items/{id}` | `DELETE` | Authenticated | Delete an item permanently (returns HTTP 204) |

### Document Processing & Knowledge Extraction (Milestone 4)
| Endpoint | Method | Security | Description |
| :--- | :--- | :--- | :--- |
| `/api/documents` | `POST` | Authenticated | Upload and process PDF or DOCX file (multipart/form-data, max 25MB) |
| `/api/documents` | `GET` | Authenticated | List authenticated user's documents (supports `status`, `format`, `search`) |
| `/api/documents/{id}` | `GET` | Authenticated | Retrieve document metadata |
| `/api/documents/{id}/status` | `GET` | Authenticated | Retrieve real-time document processing status |
| `/api/documents/{id}/content` | `GET` | Authenticated | Retrieve full extracted document text and page breakdown |
| `/api/documents/{id}/chunks` | `GET` | Authenticated | Retrieve paginated semantic chunks with page spans |
| `/api/documents/{id}/download` | `GET` | Authenticated | Download original binary document securely |
| `/api/documents/{id}/retry` | `POST` | Authenticated | Re-run extraction pipeline for failed or updated documents |
| `/api/documents/{id}` | `DELETE` | Authenticated | Permanently delete document, cascading chunks and physical disk file |

---

## Permanent Design System & Brand Identity

AI ScholarTwin adheres to a cohesive, academic SaaS visual design system defined via CSS custom properties and Tailwind CSS v4 `@theme`.

### Brand Color Palette
- **Classic Dark Blue (`#0A1828`):** Dominant dark canvas and brand anchor.
- **Turquoise (`#178582`):** Primary interactive accent (buttons, active states, focus rings, links).
- **Warm Gold (`#BFA181`):** Premium accent for academic concept notes, badges, and highlights.
- **White (`#FFFFFF`):** Crisp typography in dark mode and primary surface in light mode.

### Theme Tokens & Custom Properties
Tokens are defined in [`frontend/src/index.css`](file:///E:/projects/scholartwin-ai/frontend/src/index.css) and switch seamlessly between Dark and Light modes:

| Token | Dark Mode Value | Light Mode Value | Purpose |
| :--- | :--- | :--- | :--- |
| `--bg-page` | `#0A1828` | `#F8FAFC` | Page body background |
| `--bg-sidebar` | `#07121E` | `#FFFFFF` | Navigation sidebar background |
| `--bg-surface` | `#0F2238` | `#FFFFFF` | Cards and container surfaces |
| `--bg-surface-elevated` | `#142C48` | `#F1F5F9` | Elevated panels & inputs |
| `--border-default` | `#1B3758` | `#CBD5E1` | Card and component borders |
| `--border-subtle` | `#162C46` | `#E2E8F0` | Dividers and table lines |
| `--text-primary` | `#FFFFFF` | `#0A1828` | High-contrast headings and body text |
| `--text-secondary` | `#94A3B8` | `#334155` | Subtitles and labels |
| `--text-muted` | `#64748B` | `#64748B` | Timestamps, metadata, hints |
| `--color-brand-teal` | `#178582` | `#178582` | Primary brand accent |
| `--color-brand-gold` | `#BFA181` | `#BFA181` | Secondary brand accent |

### Reusable UI Components
All application views and future milestone features use the centralized UI library in `frontend/src/components/ui/`:
- `Button`: Primary (turquoise), Secondary (outline), Gold, Ghost, and Destructive variants with loading spinner states.
- `Card`: Composable surface with `CardHeader`, `CardTitle`, `CardDescription`, `CardContent`, and `CardFooter`.
- `Input` & `Textarea`: Accessible form controls with validation states, character counters, and turquoise focus rings.
- `Badge`: Tonal chips for `teal`, `gold`, `default`, `success`, `warning`, `error`, and `upcoming`.
- `Modal`: Accessible dialog with backdrop blur, focus trapping, and keyboard escape handling.
- `PageHeader`: Standardized responsive page headers with title, description, and action controls.
- `EmptyState`: Academic zero-state graphics, descriptions, and primary/secondary CTAs.
- `Alert`: Semantic alert banners for error, success, warning, and info notifications.
- `LoadingSkeleton` & `Spinner`: Shimmer place-holders for asynchronous state management.

### Theme Switching & Anti-Flicker Architecture
1. **Zero-Flicker Pre-hydration:** An inline script in `frontend/index.html` resolves `localStorage.getItem('scholartwin_theme')` or system `prefers-color-scheme` immediately in the `<head>`, applying `.dark` and `data-theme` to `<html>` prior to first paint.
2. **ThemeContext & Toggle:** `frontend/src/context/ThemeContext.jsx` manages runtime theme toggling and automatically synchronizes with `localStorage` and system media query changes.
3. **Accessibility:** `ThemeToggle` provides full keyboard navigation, ARIA attributes, and clear iconography (Sun/Moon).

---

## Knowledge Hub Specifications

- **Supported Types:**
  - `NOTE`: Academic note with required `title` (max 200 chars) and `content` (up to 10,000 chars).
  - `LINK`: Learning resource with required `title` (max 200 chars) and validated `url` (http/https, max 1000 chars), plus optional notes/takeaways.
- **Security & Data Isolation:**
  - Multi-tenant user isolation: Every query and mutation is strictly scoped to the authenticated user derived from JWT claims. Attempts to access or mutate another student's items return HTTP 404 (`RESOURCE_NOT_FOUND`).
  - Safe external link rendering: All external learning links are opened with `target="_blank" rel="noopener noreferrer"`.
  - Protocol safety: Backend and frontend explicitly reject unsafe URL schemes (`javascript:`, `data:`, `file:`).
- **Navigation & Workspace:**
  - Responsive `Sidebar` navigation with active route highlights and "Coming Soon" badges for future milestones (M4 AI Twin, M5 DSA Practice, M6 Career Hub).
  - Quick action modals on both Dashboard and Knowledge Hub for instant item creation.


---

## Project Structure

```
AI-ScholarTwin/
├── frontend/                   # React frontend application
│   ├── public/                 # Static assets
│   ├── src/
│   │   ├── components/         # Reusable UI components
│   │   ├── pages/              # Application views
│   │   ├── services/           # API and integration clients
│   │   ├── hooks/              # Custom React hooks
│   │   ├── assets/             # Images and styles
│   │   ├── App.jsx             # Main application container
│   │   ├── main.jsx            # Application entrypoint
│   │   └── index.css           # Tailwind CSS directives
│   ├── .env.example            # Environment template
│   ├── package.json            # Node dependencies
│   └── vite.config.js          # Vite & Tailwind configuration
├── backend/                    # Spring Boot REST backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/           # Application source code
│   │   │   └── resources/      # Application properties & resources
│   │   └── test/               # Automated tests
│   ├── pom.xml                 # Maven POM configuration
│   └── mvnw / mvnw.cmd         # Maven wrapper scripts
├── database/                   # Schema and DB documentation
│   └── README.md
├── docs/                       # Architecture & setup guides
│   ├── architecture.md
│   ├── setup-guide.md
│   └── decisions.md
├── .gitignore
└── README.md
```

---

## Quick Start

### 1. Prerequisites
- **Java 17** (Eclipse Adoptium OpenJDK 17)
- **Node.js 24+** & **npm 11+**
- **MySQL 8.x** running locally on port 3306

### 2. Running the Backend
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
The REST API will start at: `http://localhost:8080`
Health check: `http://localhost:8080/api/health`

### 3. Running the Frontend
```powershell
cd frontend
npm install
npm run dev
```
The client application will start at: `http://localhost:5173`

### 4. Running Automated Tests

#### Backend Test Suite (JUnit 5 + Spring Boot Test + MockMvc)
```powershell
cd backend
.\mvnw.cmd test
```

#### Frontend Test Suite (Vitest)
```powershell
cd frontend
npm test
```

#### Frontend Production Build
```powershell
cd frontend
npm run build
```

#### Live Milestone 4 End-to-End Verification
```powershell
powershell.exe -ExecutionPolicy Bypass -File .\verify-m4-live.ps1
```

---

## Milestone 4 Architecture & Design

### Document Processing Pipeline
1. **Validation & Security**: File size check (< 25MB), path traversal neutralization, and magic bytes verification (`%PDF` for PDF, `PK\x03\x04` for DOCX) preventing file spoofing and executable uploads.
2. **Text Extraction**:
   - Multi-page PDF extraction using Apache PDFBox 3.0.3 with page-by-page character metrics.
   - Word DOCX document extraction using Apache POI 5.3.0 extracting paragraphs, headings, and tabular data.
3. **Text Normalization**: `TextPreprocessingService` strips non-printable control characters, normalizes line breaks, and standardizes whitespace while preserving mathematical formulas and code blocks.
4. **Paragraph-Aware Chunking**: `ChunkingService` splits text along natural paragraph boundaries with sliding-window overlap (`maxChunkSize=1000`, `overlap=150`). Assigns 0-based sequential chunk indexes and page boundary tags (`startPage`, `endPage`).
5. **OCR Integration & Fallback**: `OcrService` safely evaluates local Tesseract availability via ProcessBuilder. Low-text pages (< 50 chars) trigger `ocrRequired=true`. If Tesseract is present, PDF pages are rasterized at 150 DPI and OCR is applied. If absent, the document is flagged cleanly without failing the pipeline.

### Optional Tesseract OCR Setup
To enable optional OCR on scanned PDF pages:
- **Windows**: Install Tesseract from [UB-Mannheim](https://github.com/UB-Mannheim/tesseract/wiki) to `C:\Program Files\Tesseract-OCR\tesseract.exe` and add it to `PATH`.
- **Linux / Ubuntu**: `sudo apt update && sudo apt install -y tesseract-ocr`

---

## Documentation
- [Architecture Guide](file:///e:/projects/scholartwin-ai/docs/architecture.md)
- [Developer Setup Guide](file:///e:/projects/scholartwin-ai/docs/setup-guide.md)
- [Architecture Decision Records (ADRs)](file:///e:/projects/scholartwin-ai/docs/decisions.md)
- [Database Setup Guide](file:///e:/projects/scholartwin-ai/database/README.md)
