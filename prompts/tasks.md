# Detailed Task List for User CRUD and Project Infrastructure

This task list is based on the implementation plan in `prompts/plan.md` and the requirements in `prompts/requirements.md`.

## PHASE 1: Infrastructure and Dependencies (STEP 0, 1)
1. [x] Update `pom.xml` (STEP 0)
   - [x] Add `spring-boot-starter-validation`
   - [x] Add `spring-boot-starter-web`
   - [x] Add `spring-boot-starter-security`
   - [x] Add MapStruct dependencies (`mapstruct`, `mapstruct-processor`)
   - [x] Add OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
   - [x] Add PostgreSQL driver (`postgresql`)
   - [x] Add Testcontainers dependencies (`postgresql`, `junit-jupiter`)
2. [x] JPA Auditing Configuration (STEP 1)
   - [x] Create `pl.ldz.chat.config.JpaConfig` with `@EnableJpaAuditing`
3. [x] `AbstractEntity` (STEP 1)
   - [x] Create `pl.ldz.chat.entity.AbstractEntity`
   - [x] Add fields: `id` (UUID), `version` (Long), `creationTimestamp` (Instant), `updateTimestamp` (Instant)
   - [x] Annotate with `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`
   - [x] Implement only getters for these fields
4. [x] Refactor `User` Entity (STEP 13.1)
   - [x] Update `pl.ldz.chat.entity.User` to extend `AbstractEntity`
   - [x] Remove redundant `id`, `createdAt`, `updatedAt` fields
   - [x] Remove `@PrePersist` and `@PreUpdate` methods

## PHASE 2: Data Transfer Objects and Mapping (STEP 2, 3)
5. [x] User DTOs (STEP 2)
   - [x] Create `pl.ldz.chat.dto.UserRequestDto` with Bean Validation
   - [x] Create `pl.ldz.chat.dto.UserResponseDto` with all fields (including `AbstractEntity` fields)
6. [x] User Mapper (STEP 3)
   - [x] Create `pl.ldz.chat.mapper.UserMapper` interface
   - [x] Implement mapping methods: `toResponseDto`, `toEntity`, `updateEntityFromDto`, `toResponseDtoList`
   - [x] Configure `nullValuePropertyMappingStrategy = IGNORE` for updates

## PHASE 3: Repository Layer and Tests (STEP 4, 5, 6)
7. [x] `AbstractRepository` (STEP 4)
   - [x] Create `pl.ldz.chat.repository.base.AbstractRepository` interface
   - [x] Extend `JpaRepository` and `JpaSpecificationExecutor`
8. [x] User Repository (STEP 5)
   - [x] Create `pl.ldz.chat.repository.UserRepository` extending `AbstractRepository<User, UUID>`
9. [x] Repository Integration Tests (STEP 6)
   - [x] Create `pl.ldz.chat.repository.base.AbstractRepositoryIntegrationTest` with Testcontainers
   - [x] Create `pl.ldz.chat.repository.UserRepositoryIntegrationTest` to verify CRUD and auditing

## PHASE 4: Service Layer and Tests (STEP 7, 8, 9)
10. [x] Generic Service Interface (STEP 7)
    - [x] Create `pl.ldz.chat.service.base.Service<T, ID, REQ, RES>` interface
11. [x] User Service (STEP 8)
    - [x] Create `pl.ldz.chat.service.UserService` implementing `Service`
    - [x] Inject `UserRepository` and `UserMapper` via constructor
    - [x] Add `@Service` and `@Transactional`
12. [x] Service Integration Tests (STEP 9)
    - [x] Create `pl.ldz.chat.service.base.AbstractServiceIntegrationTest` with Testcontainers
    - [x] Create `pl.ldz.chat.service.UserServiceIntegrationTest` to verify business logic and exceptions

## PHASE 5: Controller Layer and Tests (STEP 10, 11)
13. [x] User Controller (STEP 10)
    - [x] Create `pl.ldz.chat.controller.UserController`
    - [x] Implement endpoints: POST `/`, GET `/{id}`, GET `/` (paginated), PUT `/{id}`, DELETE `/{id}`
    - [x] Add OpenAPI annotations (`@Operation`, `@ApiResponse`)
14. [x] Controller Integration Tests (STEP 11)
    - [x] Create `pl.ldz.chat.controller.AbstractControllerIntegrationTest` with MockMvc and Testcontainers
    - [x] Create `pl.ldz.chat.controller.UserControllerIntegrationTest` to verify HTTP statuses and security

## PHASE 6: Security and Error Handling (STEP 12, 13)
15. [x] Security Configuration (STEP 12)
    - [x] Create `pl.ldz.chat.security.SecurityConfig`
    - [x] Configure `SecurityFilterChain` with RBAC for `VIEWER`, `USER`, `ADMIN`, `AUDITOR`
16. [x] Custom Exceptions (STEP 13.3)
    - [x] Create `pl.ldz.chat.exception.EntityNotFoundException`
17. [x] Global Error Handling (STEP 13)
    - [x] Create `pl.ldz.chat.exception.GlobalExceptionHandler`
    - [x] Handle `EntityNotFoundException`, `OptimisticLockException`, and validation errors

## PHASE 7: Final Verification
18. [x] Build and Test
    - [x] Run `mvn clean verify`
19. [x] OpenAPI/Swagger Check
    - [x] Run application and verify `/swagger-ui.html` access
20. [x] Compliance Review
    - [x] Verify 2-space indentation and project-specific guidelines (constructor injection, etc.)
