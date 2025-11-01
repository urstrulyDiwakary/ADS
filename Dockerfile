# Build stage
FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -Pproduction

# Runtime stage
FROM eclipse-temurin:23-jre-jammy
WORKDIR /app

# Install curl for health checks and create non-root user
RUN apt-get update && apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r appuser && useradd -r -g appuser appuser && \
    mkdir -p /app/logs /var/log/ads && \
    chown -R appuser:appuser /app /var/log/ads

# Copy JAR file
COPY --from=builder /app/target/ads-admin-1.0.0.jar app.jar
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Health check using actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8089/actuator/health || exit 1

# Expose port
EXPOSE 8089

# JVM optimizations for container and production
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+DisableExplicitGC -Djava.security.egd=file:/dev/./urandom"

# Start application with production profile
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
