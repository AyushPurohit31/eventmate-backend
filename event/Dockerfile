# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]