# Detailed Task List for User CRUD and Project Infrastructure

This task list is based on the implementation plan in `prompts/plan.md` and the requirements in `prompts/requirements.md`.

## PHASE 1: Infrastructure and Dependencies (STEP 0, 1)
1. [ ] Update `pom.xml` (STEP 0)
   - [ ] Add `spring-boot-starter-validation`
   - [ ] Add `spring-boot-starter-web`
   - [ ] Add `spring-boot-starter-security`
   - [ ] Add MapStruct dependencies (`mapstruct`, `mapstruct-processor`)
   - [ ] Add OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
   - [ ] Add PostgreSQL driver (`postgresql`)
   - [ ] Add Testcontainers dependencies (`postgresql`, `junit-jupiter`)
2. [ ] JPA Auditing Configuration (STEP 1)
   - [ ] Create `pl.ldz.chat.config.JpaConfig` with `@EnableJpaAuditing`
3. [ ] `AbstractEntity` (STEP 1)
   - [ ] Create `pl.ldz.chat.entity.AbstractEntity`
   - [ ] Add fields: `id` (UUID), `version` (Long), `creationTimestamp` (Instant), `updateTimestamp` (Instant)
   - [ ] Annotate with `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`
   - [ ] Implement only getters for these fields
4. [ ] Refactor `User` Entity (STEP 13.1)
   - [ ] Update `pl.ldz.chat.entity.User` to extend `AbstractEntity`
   - [ ] Remove redundant `id`, `createdAt`, `updatedAt` fields
   - [ ] Remove `@PrePersist` and `@PreUpdate` methods

## PHASE 2: Data Transfer Objects and Mapping (STEP 2, 3)
5. [ ] User DTOs (STEP 2)
   - [ ] Create `pl.ldz.chat.dto.UserRequestDto` with Bean Validation
   - [ ] Create `pl.ldz.chat.dto.UserResponseDto` with all fields (including `AbstractEntity` fields)
6. [ ] User Mapper (STEP 3)
   - [ ] Create `pl.ldz.chat.mapper.UserMapper` interface
   - [ ] Implement mapping methods: `toResponseDto`, `toEntity`, `updateEntityFromDto`, `toResponseDtoList`
   - [ ] Configure `nullValuePropertyMappingStrategy = IGNORE` for updates

## PHASE 3: Repository Layer and Tests (STEP 4, 5, 6)
7. [ ] `AbstractRepository` (STEP 4)
   - [ ] Create `pl.ldz.chat.repository.base.AbstractRepository` interface
   - [ ] Extend `JpaRepository` and `JpaSpecificationExecutor`
8. [ ] User Repository (STEP 5)
   - [ ] Create `pl.ldz.chat.repository.UserRepository` extending `AbstractRepository<User, UUID>`
9. [ ] Repository Integration Tests (STEP 6)
   - [ ] Create `pl.ldz.chat.repository.base.AbstractRepositoryIntegrationTest` with Testcontainers
   - [ ] Create `pl.ldz.chat.repository.UserRepositoryIntegrationTest` to verify CRUD and auditing

## PHASE 4: Service Layer and Tests (STEP 7, 8, 9)
10. [ ] Generic Service Interface (STEP 7)
    - [ ] Create `pl.ldz.chat.service.base.Service<T, ID, REQ, RES>` interface
11. [ ] User Service (STEP 8)
    - [ ] Create `pl.ldz.chat.service.UserService` implementing `Service`
    - [ ] Inject `UserRepository` and `UserMapper` via constructor
    - [ ] Add `@Service` and `@Transactional`
12. [ ] Service Integration Tests (STEP 9)
    - [ ] Create `pl.ldz.chat.service.base.AbstractServiceIntegrationTest` with Testcontainers
    - [ ] Create `pl.ldz.chat.service.UserServiceIntegrationTest` to verify business logic and exceptions

## PHASE 5: Controller Layer and Tests (STEP 10, 11)
13. [ ] User Controller (STEP 10)
    - [ ] Create `pl.ldz.chat.controller.UserController`
    - [ ] Implement endpoints: POST `/`, GET `/{id}`, GET `/` (paginated), PUT `/{id}`, DELETE `/{id}`
    - [ ] Add OpenAPI annotations (`@Operation`, `@ApiResponse`)
14. [ ] Controller Integration Tests (STEP 11)
    - [ ] Create `pl.ldz.chat.controller.AbstractControllerIntegrationTest` with MockMvc and Testcontainers
    - [ ] Create `pl.ldz.chat.controller.UserControllerIntegrationTest` to verify HTTP statuses and security

## PHASE 6: Security and Error Handling (STEP 12, 13)
15. [ ] Security Configuration (STEP 12)
    - [ ] Create `pl.ldz.chat.security.SecurityConfig`
    - [ ] Configure `SecurityFilterChain` with RBAC for `VIEWER`, `USER`, `ADMIN`, `AUDITOR`
16. [ ] Custom Exceptions (STEP 13.3)
    - [ ] Create `pl.ldz.chat.exception.EntityNotFoundException`
17. [ ] Global Error Handling (STEP 13)
    - [ ] Create `pl.ldz.chat.exception.GlobalExceptionHandler`
    - [ ] Handle `EntityNotFoundException`, `OptimisticLockException`, and validation errors

## PHASE 7: Final Verification
18. [ ] Build and Test
    - [ ] Run `mvn clean verify`
19. [ ] OpenAPI/Swagger Check
    - [ ] Run application and verify `/swagger-ui.html` access
20. [ ] Compliance Review
    - [ ] Verify 2-space indentation and project-specific guidelines (constructor injection, etc.)
