# Multi-stage Dockerfile for Slotify Backend

# Stage 1: Build
FROM gradle:8.7-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY back-end/gradle/ back-end/gradle/
COPY back-end/gradlew back-end/gradlew.bat back-end/settings.gradle back-end/build.gradle /app/back-end/

# Copy source code
COPY back-end/src /app/back-end/src

# Build the application
WORKDIR /app/back-end
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from build stage
COPY --from=build /app/back-end/build/libs/*.jar app.jar

# Create uploads directory
RUN mkdir -p /tmp/uploads && chown -R spring:spring /tmp/uploads

# Switch to non-root user
USER spring:spring

# Expose port (Railway will override this with PORT env var)
EXPOSE 8080

# Set production profile by default
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
# Use shell form to allow environment variable substitution
ENTRYPOINT exec java -Dserver.port=${PORT:-8080} -jar app.jar
