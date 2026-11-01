# AI ScholarTwin — Developer Setup Guide

This guide details the step-by-step procedures to configure, run, and verify the frontend and backend services.

---

## Prerequisites
- **Java:** JDK 17 (Verified: Eclipse Adoptium OpenJDK 17)
- **Node.js:** Node 24.x LTS/Current with npm 11.x
- **MySQL:** MySQL 8.x Server (Windows Service: `MySQL80` on port 3306)
- **Git:** Git for Windows

---

## 1. Backend Setup & Startup

### Configuration
1. Copy `backend/.env.example` to `backend/.env` (or pass environment variables directly):
   ```properties
   SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ai_scholartwin?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   SPRING_DATASOURCE_USERNAME=scholartwin_user
   SPRING_DATASOURCE_PASSWORD=scholartwin_password
   ```

### Compile & Run
In PowerShell, navigate to the `backend` folder:
```powershell
cd backend
# Build and compile
.\mvnw.cmd clean compile

# Run automated tests
.\mvnw.cmd test

# Start the Spring Boot application on port 8080
.\mvnw.cmd spring-boot:run
```

Verify backend health by visiting:
[http://localhost:8080/api/health](http://localhost:8080/api/health)

---

## 2. Frontend Setup & Startup

### Installation & Run
In PowerShell, navigate to the `frontend` folder:
```powershell
cd frontend
# Install dependencies
npm install

# Start Vite development server
npm run dev
```

Visit the frontend at [http://localhost:5173](http://localhost:5173). Requests to `/api/*` will automatically be proxied to the backend on `http://localhost:8080`.

---

## 3. Production Build Verification

### Frontend Build
```powershell
cd frontend
npm run build
```

### Backend Package
```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
```
