# Project Guidelines

## Overview

This document provides essential guidelines for working on this project. Follow these rules to ensure consistency, maintainability, and clean architecture.

---

## Project Structure

### Base Package

`pl.ldz.chat`

### Package Structure

- `pl.ldz.chat.controller` – REST endpoints (no business logic)
- `pl.ldz.chat.service` – business logic layer
- `pl.ldz.chat.repository` – Spring Data access layer
- `pl.ldz.chat.dto` – request and response models
- `pl.ldz.chat.security` – authentication and authorization logic
- `pl.ldz.chat.config` – application configuration
- `pl.ldz.chat.domain` – JPA entities
- `pl.ldz.chat.exception` – exception handling

### Rules

- Keep controllers thin – only handle request/response
- Place all business logic in services
- Use DTOs for all API communication
- Do not expose entities directly in APIs

---

## Persistence & Concurrency

### Optimistic Locking

- Use Optimistic Locking for all mutable entities
- Add a version field to entities:

```java
@Version
private Long version;

---


## Build & Run

This project uses Maven.

### Common Commands

* Build JAR:

  ```
mvn clean package
  ```

* Run application locally:

  ```
java -jar target/*.jar
  ```

* Run unit tests:

  ```
mvn test
  ```

* Run integration tests:

  ```
mvn failsafe:integration-test failsafe:verify
  ```

* Run all checks:

  ```
mvn verify
  ```

---

## Docker Setup

### Build Docker Image

Example `Dockerfile`:

```
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Build image:

```
docker build -t app:latest .
```

Run container:

```
docker run -p 8080:8080 app:latest
```

---

## Docker Compose (App + PostgreSQL)

Example `docker-compose.yml`:

```
version: "3.9"

services:
db:
image: postgres:15
container_name: postgres-db
environment:
POSTGRES_DB: app
POSTGRES_USER: app
POSTGRES_PASSWORD: app
ports:
- "5432:5432"
volumes:
- postgres-data:/var/lib/postgresql/data

app:
image: app:latest
container_name: spring-app
depends_on:
- db
ports:
- "8080:8080"
environment:
SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/app
SPRING_DATASOURCE_USERNAME: app
SPRING_DATASOURCE_PASSWORD: app

volumes:
postgres-data:
```

Run everything:

```
docker compose up --build
```

---

## Testing

### Unit Tests

* Location: `src/test/java`
* Must be fast and isolated
* Mock all external dependencies
* Focus on business logic

### Integration Tests

* Location: `src/integration-test/java`
* Use full Spring context
* Use Abstract classes for decreasing code
* Validate end-to-end flows (request → response)

---

## Code Style

* Use **2 spaces** for indentation
* Prefer constructor injection over field injection
* Keep classes small and focused
* Keep methods short and readable
* Use clear, descriptive naming

---

## Security

### Roles

* `VIEWER` – limited read-only access
* `USER` – standard application access
* `ADMIN` – full access to all features
* `AUDITOR` – read-only access to all resources

---

## Frameworks

* Spring Boot 3
* Spring Web (REST)
* Spring Data
* Spring Security
* Springdoc OpenAPI (Swagger)

---
## Use Project Lombok
* Use Lombok to reduce boilerplate code.
* Enable annotation processing for your IDE to generate boilerplate code for you.
* When adding builder to a class, if the class extends another class, add `@SuperBuilder` for the builder.
---

## API Documentation

* Use OpenAPI annotations for all endpoints
* Ensure Swagger UI is available
* Clearly define request and response models
* Keep documentation up to date

---

## Best Practices

* Validate all input data
* Write tests for all layers
* Follow separation of concerns strictly
* Avoid duplication of logic
* Keep code simple and maintainable

---

## Developer Workflow

1. Implement feature in the service layer
2. Expose functionality via controller
3. Add/update DTOs as needed
4. Write unit tests
5. Add integration tests if applicable
6. Build application:

```
mvn clean package
```
7. Run locally or via Docker
8. Ensure API is documented via Swagger

---