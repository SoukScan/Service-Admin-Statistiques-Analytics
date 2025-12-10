# Multi-stage Dockerfile for Admin Moderation Service
# Stage 1: Build stage with Maven
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Install Maven in builder stage
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml first (for better caching)
COPY pom.xml .

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime stage with JRE only
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR from builder stage
COPY --from=builder /app/target/admin-moderation-service-1.0.0.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8090
EXPOSE 8090

# Health check using actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

# Start application with docker profile
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]
