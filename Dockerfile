# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies first (caching layer)
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the JAR, skipping tests for faster build
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for file uploads/certificates (needs persistence)
RUN mkdir -p /app/uploads/certificates

# Copy JAR from builder
COPY --from=builder /app/target/digital-certificate-system-0.0.1-SNAPSHOT.jar app.jar

# Expose web port
EXPOSE 8080

# Run
ENTRYPOINT ["java", "-jar", "app.jar"]

