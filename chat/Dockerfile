# Stage 1: Build the frontend
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

# Install dependencies first (layer caching)
COPY frontend/package.json ./
RUN npm install

# Copy source and build
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy built frontend static files into Spring Boot static resources
COPY --from=frontend-builder /app/src/main/resources/static ./src/main/resources/static

# Copy Java source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 3: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create a non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
