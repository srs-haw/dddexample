# Multi-stage build for optimized production image

# Stage 1: Build Backend
FROM openjdk:21-jdk-slim AS backend-build
WORKDIR /app
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle/ gradle/
COPY src/ src/
RUN ./gradlew build -x test --no-daemon

# Stage 2: Production Image
FROM openjdk:21-jre-slim
WORKDIR /app

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copy built JAR
COPY --from=backend-build /app/build/libs/*.jar app.jar

# Create non-root user
RUN addgroup --system appgroup && \
    adduser --system --group appuser
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]