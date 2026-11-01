# AI ScholarTwin

**AI ScholarTwin** is an AI-powered personalized academic and engineering learning assistant. It organizes student knowledge, understands course and research materials, assists in problem-solving, tracks algorithmic (DSA) coding progress, analyzes resume and career readiness, and devises personalized learning curricula.

> **Status:** Milestone 1 — Project Foundation & Architecture

---

## Technology Stack

- **Frontend:** React 19, Vite, Tailwind CSS v4
- **Backend:** Java 17, Spring Boot 3.4.3
- **ORM / Persistence:** Spring Data JPA, Hibernate
- **Database:** MySQL 8.x
- **Build Tools:** Maven Wrapper (`mvnw`), npm
- **Future AI Integration:** Gemini API (planned for subsequent milestones)

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

---

## Documentation
- [Architecture Guide](file:///e:/projects/scholartwin-ai/docs/architecture.md)
- [Developer Setup Guide](file:///e:/projects/scholartwin-ai/docs/setup-guide.md)
- [Architecture Decision Records (ADRs)](file:///e:/projects/scholartwin-ai/docs/decisions.md)
- [Database Setup Guide](file:///e:/projects/scholartwin-ai/database/README.md)
