# Project Guidelines

## Purpose

These guidelines define how to structure, implement, test, and evolve this project consistently.

The project is a Spring Boot application in the `pl.ldz.chat` base package. Keep the codebase simple, modular, and easy to extend.

---

## General Principles

- Prefer clarity over cleverness.
- Keep changes small and focused.
- Follow existing conventions when modifying code.
- Avoid premature abstraction.
- Keep business rules explicit and easy to test.
- Do not introduce unnecessary dependencies.

---

## Project Structure

Use the following package layout under `pl.ldz.chat` when the codebase grows:

- `pl.ldz.chat.controller` — HTTP endpoints and request/response handling
- `pl.ldz.chat.service` — business logic and use cases
- `pl.ldz.chat.repository` — persistence access
- `pl.ldz.chat.entity` — JPA entities
- `pl.ldz.chat.dto` — API request/response models
- `pl.ldz.chat.config` — application configuration
- `pl.ldz.chat.security` — authentication and authorization concerns
- `pl.ldz.chat.exception` — custom exceptions and global error handling

### Structural rules

- Keep controllers thin.
- Put business logic in services.
- Do not expose entities directly in API responses.
- Use DTOs for communication across API boundaries.
- Keep repositories focused on persistence only.
- Avoid mixing configuration, business logic, and web concerns in the same class.

---

## Code Style

- Use Java 21 language features where they improve readability.
- Prefer constructor injection over field injection.
- Keep classes small and focused on a single responsibility.
- Keep methods short and readable.
- Use descriptive names for packages, classes, methods, and variables.
- Avoid duplication; extract reusable logic only when it is genuinely shared.
- Prefer immutable data where practical.
- If Lombok is used, keep annotations minimal and readable.
- Use **2 spaces** for indentation in Java and XML files.

### Formatting

- Follow the formatter and inspection settings configured in the IDE/project.
- Keep indentation consistent with the surrounding code.
- Write clean, readable code rather than compact code.

---

## API Design

- Design endpoints around business use cases.
- Use REST conventions consistently.
- Keep request and response contracts stable.
- Validate all incoming request data.
- Return meaningful HTTP status codes.
- Handle errors in a centralized way.
- Document public endpoints when API documentation is introduced or updated.

### Controllers

- Accept request data via DTOs.
- Convert between DTOs and internal models in the service or mapping layer.
- Do not place business rules in controllers.
- Keep controller methods focused on orchestration only.

---

## Service Layer

- Place application logic in services.
- Services should orchestrate validation, persistence, and domain behavior.
- Keep services independent from web concerns.
- Make service methods expressive and testable.
- Prefer domain-focused method names over technical names.

---

## Persistence

- Keep entity classes focused on persistence concerns.
- Model relationships carefully and intentionally.
- Avoid leaking lazy-loading or persistence details into the API layer.
- Use transactions where consistency is required.
- Add optimistic locking with `@Version` for entities that may be updated concurrently.
- Do not overuse cascading without understanding the impact.

---

## Validation and Error Handling

- Validate input as early as possible.
- Use bean validation for request DTOs where appropriate.
- Throw domain-specific or application-specific exceptions when needed.
- Map exceptions to consistent API error responses.
- Keep error messages clear, actionable, and safe for clients.

---

## Testing Strategy

### Unit tests

- Put unit tests in `src/test/java`.
- Keep them fast and isolated.
- Mock external dependencies.
- Focus on business logic and edge cases.

### Integration tests

- Use integration tests for Spring wiring, persistence, and end-to-end flows.
- Prefer a separate integration-test structure only if the project later adopts it consistently.
- Use real application context only where it adds value.
- Keep test setup understandable and maintainable.

### Testing rules

- Add tests for new behavior.
- Update tests when behavior changes.
- Prefer readable assertions over overly generic checks.
- Test one behavior per test when possible.

---

## Build and Run

This project uses Maven.

Common commands:

```bash
mvn clean package
```

```bash
mvn test
```

```bash
mvn verify
```

Run the application locally with the standard Spring Boot Maven workflow used by the project.

When adding new build steps or plugins, keep them documented and aligned with the project's current setup.

---

## Docker

The project uses Docker Compose to run infrastructure dependencies locally. Docker must be installed and running before starting the database.

### docker-compose.yml

Place this file in the project root:

```yaml
services:
  db:
    image: postgres:16
    container_name: chat_db
    environment:
      POSTGRES_DB: chat
      POSTGRES_USER: chat
      POSTGRES_PASSWORD: chat
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Common Docker Compose commands

Start the database before running the application:

```bash
docker compose up -d
```

Stop the database when done:

```bash
docker compose down
```

Wipe the database volume entirely (destructive — all local data will be lost):

```bash
docker compose down -v
```

### Notes

- The named volume `postgres_data` persists data between `up`/`down` cycles so local state is not lost accidentally.
- Credentials in `docker-compose.yml` are for local development only. Never commit production credentials. Use environment variables or a secrets manager for non-local environments.
- Do not log or expose the database password in application output or error messages.

---

## Dependencies

- Prefer Spring Boot starters over manually managing many low-level dependencies.
- Add new dependencies only when they clearly support a project requirement.
- Keep the dependency set lean.
- Review the impact of every new library on build size, maintenance, and security.

### PostgreSQL driver

Add the PostgreSQL JDBC driver to `pom.xml`:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

The version is managed automatically by the Spring Boot parent POM. Do not specify it manually unless there is a specific reason to pin a version.

---

## Database Configuration

### application.properties

Add the following to `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chat
spring.datasource.username=chat
spring.datasource.password=chat
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

### Schema management

- Use `ddl-auto=validate` in all environments except the very earliest development phase.
- Do not use `ddl-auto=create`, `create-drop`, or `update` once the schema has stabilized.
- Prefer Flyway or Liquibase for managing schema changes as the project grows. Migrations should be versioned, reviewed, and committed alongside the code that requires them.
- Keep migration scripts small, focused, and reversible where possible.

### application.properties for tests

Integration tests that require a real database should use a dedicated test datasource. Consider using [Testcontainers](https://testcontainers.com/) to spin up a PostgreSQL instance automatically during the test run:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <scope>test</scope>
</dependency>
```

---

## Security

- Treat all external input as untrusted.
- Validate and sanitize user-provided data.
- Do not log secrets, credentials, or tokens.
- Keep authentication and authorization logic isolated from business logic.
- Use role-based access control only where it is needed.
- Review access rules whenever endpoints or permissions change.

### Security roles

Use the following roles consistently when implementing authorization:

- `VIEWER` — limited read-only access
- `USER` — standard application access
- `ADMIN` — full access to all features
- `AUDITOR` — read-only access to all resources

### Spring Security conventions

- Prefer `hasRole(...)` for role-based checks in application code.
- Use `hasAuthority(...)` only when a finer-grained permission model is required.
- Keep role checks explicit and easy to understand.
- Apply authorization at the boundary of secured operations, not deep inside business logic.
- Keep role naming consistent across configuration, annotations, tests, and documentation.
- Use the least-privilege principle: grant only the access required for the feature.
- Update access rules together with API or business changes.

### Authorization rules

- Public endpoints should remain explicitly public.
- Read-only operations should typically allow `VIEWER`, `USER`, `ADMIN`, or `AUDITOR` depending on the use case.
- Write operations should usually require `USER` or `ADMIN`.
- Administrative operations should require `ADMIN`.
- Audit-only access should be limited to `AUDITOR` where appropriate.

---

## Documentation

- Keep README and project guidelines aligned with the actual codebase.
- Document important architectural decisions when they affect future work.
- Add concise code comments only when the intent is not obvious from the code itself.
- Prefer self-explanatory code over long comments.

---

## Git and Change Management

- Make changes in small, reviewable steps.
- Keep commits focused on one logical purpose.
- Avoid unrelated refactoring in feature work unless it is necessary.
- Update tests and documentation together with code changes when appropriate.

---

## Working Rule of Thumb

When implementing a feature:

1. Identify the business need.
2. Add or update the service logic.
3. Expose the behavior through the controller if needed.
4. Add validation and error handling.
5. Write or update tests.
6. Keep the codebase consistent with the existing structure.

---

## Project-Specific Guidance

This repository is currently minimal, so prefer:
- simple solutions over layered complexity,
- conventions that match Spring Boot defaults,
- only the packages and abstractions that the codebase actually needs right now.

As the project grows, expand the structure gradually and only where it improves maintainability.
