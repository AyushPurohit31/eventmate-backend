# Multi-stage build for better security and smaller image size
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

# Copy the built jar file from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8081

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]