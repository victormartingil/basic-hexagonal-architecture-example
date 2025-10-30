# ============================================================================
# Multi-Stage Dockerfile para Hexarch - Optimizado para Producción
# ============================================================================
#
# BENEFITS:
# - Imagen final pequeña (~200MB vs ~800MB con single-stage)
# - Sin Maven ni build tools en producción
# - Solo JRE (no JDK completo)
# - Capas cacheables para builds más rápidos
#
# BUILD:
#   docker build -t hexarch:latest .
#
# RUN:
#   docker run -p 8080:8080 \
#     -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/hexarch_db \
#     -e JWT_SECRET=your-production-secret \
#     hexarch:latest
#
# ============================================================================

# ============================================================================
# STAGE 1: BUILD
# ============================================================================
# Usa imagen con Maven y JDK para compilar el proyecto
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

# Metadata
LABEL stage=builder
LABEL description="Build stage for Hexarch microservice"

# Working directory
WORKDIR /app

# Copy solo pom.xml primero (para cachear dependencies)
COPY pom.xml .

# Download dependencies (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests para build rápido)
# Tests se ejecutan en CI/CD, no en Docker build
RUN mvn clean package -DskipTests -B

# Verify JAR was created
RUN ls -lh target/*.jar

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
# Imagen minimalista solo con JRE (sin Maven ni build tools)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadata
LABEL maintainer="Hexarch Team <team@hexarch.com>"
LABEL version="1.0.0"
LABEL description="Hexarch User Service - Spring Boot microservice with Hexagonal Architecture"

# Install curl (para health checks)
RUN apk add --no-cache curl

# Create non-root user para seguridad
# BEST PRACTICE: No ejecutar aplicaciones como root
RUN addgroup -S spring && adduser -S spring -G spring

# Working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check (Kubernetes-compatible)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# JVM tuning para contenedores
# -XX:+UseContainerSupport: Detecta límites de CPU/memoria del contenedor
# -XX:MaxRAMPercentage=75.0: Usa máx 75% de RAM del contenedor
# -Djava.security.egd: Mejora performance de SecureRandom
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

# Run application
# exec forma previene PID 1 issues con signals (SIGTERM)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
