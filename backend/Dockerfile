# ==============================================================================
# AI ScholarTwin — Backend Production Dockerfile (Multi-Stage Build)
# ==============================================================================

# Stage 1: Build JAR with Maven & Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache Maven dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime Environment with Eclipse Temurin JRE 17 & Tesseract OCR
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Install Tesseract OCR and English language data for document OCR processing
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-eng \
        wget \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Create dedicated non-root application user for defense-in-depth
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser

# Create persistent storage directories with appropriate permissions
RUN mkdir -p /app/uploads/documents && \
    chown -R appuser:appgroup /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/target/*.jar /app/app.jar

# Switch to unprivileged user
USER appuser

# Configure environment defaults (overridden at runtime via Docker Compose or K8s)
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    DOCUMENT_STORAGE_DIR=/app/uploads/documents \
    TESSERACT_PATH=/usr/bin/tesseract

EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
