# Implementation Plan for User CRUD and Project Infrastructure

This plan is based on the requirements defined in `prompts/requirements.md`. The goal is to implement full CRUD functionality for the `User` entity while establishing a scalable architecture using abstract base classes for entities, repositories, services, and controllers.

## PHASE 1: Infrastructure and Dependencies
1.  **Update `pom.xml`**:
    -   Add `spring-boot-starter-validation`
    -   Add `spring-boot-starter-web`
    -   Add `spring-boot-starter-security`
    -   Add MapStruct dependencies (`mapstruct`, `mapstruct-processor`)
    -   Add OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
    -   Add PostgreSQL and Testcontainers dependencies (`postgresql`, `testcontainers-postgresql`, `junit-jupiter`)
2.  **JPA Auditing Configuration**:
    -   Create `pl.ldz.chat.config.JpaConfig` with `@EnableJpaAuditing`.

## PHASE 2: Base Classes (STEP 1, 4, 7)
1.  **`AbstractEntity` (STEP 1)**:
    -   Location: `pl.ldz.chat.entity.base.AbstractEntity`
    -   Fields: `id`, `version`, `creationTimestamp`, `updateTimestamp`.
    -   Refactor existing entities to extend `AbstractEntity` (remove duplicated fields).
2.  **`AbstractRepository` (STEP 4)**:
    -   Location: `pl.ldz.chat.repository.base.AbstractRepository`
    -   Extend `JpaRepository` and `JpaSpecificationExecutor`.
3.  **Base `Service` Interface (STEP 7)**:
    -   Location: `pl.ldz.chat.service.base.Service`
    -   Generic CRUD methods: `create`, `getById`, `getAll`, `update`, `delete`.

## PHASE 3: User Implementation (STEP 2, 3, 5, 8, 10)
1.  **User DTOs (STEP 2)**:
    -   Location: `pl.ldz.chat.dto.UserRequestDto` (with Bean Validation), `pl.ldz.chat.dto.UserResponseDto`.
2.  **User Mapper (STEP 3)**:
    -   Location: `pl.ldz.chat.mapper.UserMapper` (MapStruct).
3.  **User Repository (STEP 5)**:
    -   Location: `pl.ldz.chat.repository.UserRepository` (Extends `AbstractRepository`).
4.  **User Service (STEP 8)**:
    -   Location: `pl.ldz.chat.service.UserService` (implements `Service`).
5.  **User Controller (STEP 10)**:
    -   Location: `pl.ldz.chat.controller.UserController` (REST endpoints, OpenAPI annotations, RBAC).

## PHASE 4: Integration Testing (STEP 6, 9, 11)
1.  **Repository Integration Tests (STEP 6)**:
    -   `AbstractRepositoryIntegrationTest` with Testcontainers.
    -   `UserRepositoryIntegrationTest`.
2.  **Service Integration Tests (STEP 9)**:
    -   `AbstractServiceIntegrationTest` with Testcontainers.
    -   `UserServiceIntegrationTest`.
3.  **Controller Integration Tests (STEP 11)**:
    -   `AbstractControllerIntegrationTest` with `MockMvc` and Testcontainers.
    -   `UserControllerIntegrationTest`.

## PHASE 5: Security and Error Handling (STEP 12, 13)
1.  **Security Configuration (STEP 12)**:
    -   Location: `pl.ldz.chat.security.SecurityConfig`.
    -   Define `SecurityFilterChain` and RBAC rules.
2.  **Global Error Handling (STEP 13)**:
    -   Location: `pl.ldz.chat.exception.GlobalExceptionHandler` (`@RestControllerAdvice`).
    -   Custom `EntityNotFoundException`.
    -   Handle `OptimisticLockException` and validation errors.

## PHASE 6: Final Verification
1.  Build project with `mvn clean verify`.
2.  Ensure all integration tests pass.
3.  Check OpenAPI documentation at `/swagger-ui.html`.
