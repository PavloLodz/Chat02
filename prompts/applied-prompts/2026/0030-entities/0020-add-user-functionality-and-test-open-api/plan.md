# Implementation Plan for User CRUD and Project Infrastructure

This plan is based on the requirements defined in `prompts/requirements.md` and the draft in `prompts/plan-draft.md`. The goal is to implement full CRUD functionality for the `User` entity while establishing a scalable architecture using abstract base classes for entities, repositories, services, and controllers.

## PHASE 1: Infrastructure and Dependencies (STEP 0, 1)
1.  **Update `pom.xml` (STEP 0)**:
    -   Add `spring-boot-starter-validation`
    -   Add `spring-boot-starter-web`
    -   Add `spring-boot-starter-security`
    -   Add MapStruct dependencies (`mapstruct`, `mapstruct-processor`)
    -   Add OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
    -   Add PostgreSQL and Testcontainers dependencies (`postgresql`, `testcontainers-postgresql`, `junit-jupiter`)
2.  **JPA Auditing Configuration (STEP 1)**:
    -   Create `pl.ldz.chat.config.JpaConfig` with `@EnableJpaAuditing`.
3.  **`AbstractEntity` (STEP 1)**:
    -   Location: `pl.ldz.chat.entity.base.AbstractEntity`
    -   Fields: `id` (UUID), `version` (Long), `creationTimestamp` (Instant), `updateTimestamp` (Instant).
    -   Use `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`, `@CreatedDate`, `@LastModifiedDate`.
    -   Expose only getters for these fields.
4.  **Refactor `User` Entity (STEP 13.1)**:
    -   Update `pl.ldz.chat.entity.User` to extend `AbstractEntity`.
    -   Remove redundant `id`, `createdAt`, `updatedAt` fields and `@PrePersist`/`@PreUpdate` methods.

## PHASE 2: Data Transfer Objects and Mapping (STEP 2, 3)
1.  **User DTOs (STEP 2)**:
    -   `pl.ldz.chat.dto.UserRequestDto`: include writable fields (username, email, passwordHash, displayName, avatarUrl, online) with `@NotBlank`, `@Email`, etc.
    -   `pl.ldz.chat.dto.UserResponseDto`: include all fields including those from `AbstractEntity`.
2.  **User Mapper (STEP 3)**:
    -   `pl.ldz.chat.mapper.UserMapper`: MapStruct interface with `toResponseDto`, `toEntity`, `updateEntityFromDto`, and `toResponseDtoList`.
    -   Configure `nullValuePropertyMappingStrategy = IGNORE` for updates.

## PHASE 3: Repository Layer and Tests (STEP 4, 5, 6)
1.  **`AbstractRepository` (STEP 4)**:
    -   Location: `pl.ldz.chat.repository.base.AbstractRepository`
    -   Extend `JpaRepository` and `JpaSpecificationExecutor`.
2.  **User Repository (STEP 5)**:
    -   Location: `pl.ldz.chat.repository.UserRepository`
    -   Extend `AbstractRepository<User, UUID>`.
3.  **Repository Integration Tests (STEP 6)**:
    -   `pl.ldz.chat.repository.base.AbstractRepositoryIntegrationTest`: base class with `@DataJpaTest`, `@AutoConfigureTestDatabase(Replace.NONE)`, and PostgreSQL Testcontainer.
    -   `pl.ldz.chat.repository.UserRepositoryIntegrationTest`: verify CRUD, custom queries, and auditing timestamps.

## PHASE 4: Service Layer and Tests (STEP 7, 8, 9)
1.  **Generic Service Interface (STEP 7)**:
    -   `pl.ldz.chat.service.base.Service<T, ID, REQ, RES>`: define `create`, `getById`, `getAll`, `update`, `delete`.
2.  **User Service (STEP 8)**:
    -   `pl.ldz.chat.service.UserService`: implement CRUD using `UserRepository` and `UserMapper`.
    -   Use `@Service` and `@Transactional`.
3.  **Service Integration Tests (STEP 9)**:
    -   `pl.ldz.chat.service.base.AbstractServiceIntegrationTest`: base class with `@SpringBootTest`, `@Testcontainers`, and PostgreSQL Testcontainer.
    -   `pl.ldz.chat.service.UserServiceIntegrationTest`: verify business logic, exception handling, and pagination.

## PHASE 5: Controller Layer and Tests (STEP 10, 11)
1.  **User Controller (STEP 10)**:
    -   `pl.ldz.chat.controller.UserController`: `@RestController`, `@RequestMapping("/api/v1/users")`.
    -   Implement endpoints: POST `/`, GET `/{id}`, GET `/` (paginated), PUT `/{id}`, DELETE `/{id}`.
    -   Use `@Valid`, `@Operation`, `@PreAuthorize`.
2.  **Controller Integration Tests (STEP 11)**:
    -   `pl.ldz.chat.controller.AbstractControllerIntegrationTest`: base class with `MockMvc`, `ObjectMapper`, and PostgreSQL Testcontainer.
    -   `pl.ldz.chat.controller.UserControllerIntegrationTest`: verify HTTP status codes, JSON responses, validation, and security.

## PHASE 6: Security and Error Handling (STEP 12, 13)
1.  **Security Configuration (STEP 12)**:
    -   `pl.ldz.chat.security.SecurityConfig`: define `SecurityFilterChain`, configure RBAC for roles `VIEWER`, `USER`, `ADMIN`, `AUDITOR`.
2.  **Global Error Handling (STEP 13)**:
    -   `pl.ldz.chat.exception.GlobalExceptionHandler`: `@RestControllerAdvice`.
    -   Handle `EntityNotFoundException`, `OptimisticLockException`, and `MethodArgumentNotValidException`.
3.  **Custom Exceptions (STEP 13.3)**:
    -   Create `pl.ldz.chat.exception.EntityNotFoundException`.

## PHASE 7: Final Verification
1.  **Build and Test**:
    -   Run `mvn clean verify`.
2.  **OpenAPI/Swagger**:
    -   Run application and check `/swagger-ui.html`.
3.  **Compliance**:
    -   Ensure 2-space indentation and constructor injection are used throughout.
