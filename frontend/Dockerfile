# ==============================================================================
# AI ScholarTwin — Frontend Production Dockerfile (Multi-Stage Build)
# ==============================================================================

# Stage 1: Build static assets using Node 20 Alpine
FROM node:20-alpine AS builder
WORKDIR /app

# Install dependencies (utilizing Docker layer caching)
COPY package.json package-lock.json ./
RUN npm ci

# Copy frontend source files
COPY . ./

# Build production bundle using Vite
RUN npm run build

# Stage 2: Serve via Nginx Alpine with Reverse Proxy
FROM nginx:alpine AS runner

# Remove default Nginx welcome configuration
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copy custom Nginx configuration with reverse proxy & upload size rules
COPY nginx.conf /etc/nginx/conf.d/scholartwin.conf

# Copy compiled production bundle from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

# Health check
HEALTHCHECK --interval=15s --timeout=5s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
