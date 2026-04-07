# Plan for Implementing CRUD Functionality for PersonalChat

This plan outlines the steps required to implement full CRUD functionality for the PersonalChat entity, based on the requirements defined in `prompts/requirements.md` and following the project guidelines.

## Phase 1: Verification and Base Infrastructure
- [ ] Verify the existence and correctness of base classes:
  - `pl.ldz.chat.entity.base.AbstractEntity` (id, version, audit fields)
  - `pl.ldz.chat.repository.base.AbstractRepository` (extends JpaRepository, JpaSpecificationExecutor)
  - `pl.ldz.chat.service.base.CrudService` (standard CRUD interface)
  - `pl.ldz.chat.exception.GlobalExceptionHandler` (handles EntityNotFoundException, OptimisticLockException, etc.)
  - `pl.ldz.chat.ITSharedPostgres` (shared Testcontainers configuration)
- [ ] Ensure JPA Auditing is enabled (check for `@EnableJpaAuditing`).

## Phase 2: DTO and Mapper Implementation
- [ ] Create `PersonalChatRequestDto` in `pl.ldz.chat.dto`:
  - Fields: `user1Id` (UUID), `user2Id` (UUID).
  - Add bean validation annotations (`@NotNull`).
- [ ] Create `PersonalChatResponseDto` in `pl.ldz.chat.dto`:
  - Fields: `id`, `user1` (UserResponseDto), `user2` (UserResponseDto), `version`, `creationTimestamp`, `updateTimestamp`.
- [ ] Create `UserResponseDto` (if missing) to represent participant details without collections.
- [ ] Create `PersonalChatMapper` in `pl.ldz.chat.mapper`:
  - Use MapStruct with `componentModel = "spring"`.
  - Implement `toEntity`, `toResponseDto`, `updateEntityFromDto`, and `toResponseDtoList`.
  - Ensure ID and audit fields are ignored when mapping to Entity.
  - Handle conversion from `UUID` (user1Id, user2Id) to `User` entity (may require `@Context` or custom mapping methods).

## Phase 3: Persistence and Service Layer
- [ ] Create `PersonalChatRepository` in `pl.ldz.chat.repository`:
  - Extend `AbstractRepository<PersonalChat, UUID>`.
  - Apply `@EntityGraph` or join fetches on `user1` and `user2` to prevent N+1 issues.
- [ ] Create `PersonalChatService` in `pl.ldz.chat.service`:
  - Implement `CrudService<PersonalChat, UUID, PersonalChatRequestDto, PersonalChatResponseDto>`.
  - Use constructor injection for repository and mapper.
  - Apply `@Transactional` and `@Transactional(readOnly = true)`.
  - Implement logic to fetch `User` entities by ID before saving/updating.
  - Handle `EntityNotFoundException`.

## Phase 4: API and Security
- [ ] Create `PersonalChatController` in `pl.ldz.chat.controller`:
  - Implement endpoints: POST, GET (by ID), GET (paginated), PUT, DELETE.
  - Use `@PreAuthorize` for RBAC:
    - POST: `hasAnyRole('USER', 'ADMIN')`
    - GET (all): `hasRole('ADMIN')` (or logic to filter by current user)
    - GET (by ID): `hasAnyRole('ADMIN')` or check if user is participant.
    - PUT/DELETE: `hasRole('ADMIN')`.
  - Add OpenAPI documentation annotations (`@Operation`, `@ApiResponse`, etc.).
  - Use `@ParameterObject Pageable pageable` for pagination.
- [ ] Update `pl.ldz.chat.security.SecurityConfig`:
  - Add endpoint security rules for `/api/v1/personal-chats/**` if needed.

## Phase 5: Testing and Validation
- [ ] Implement `PersonalChatServiceIT` in `src/integration-test/java/pl/ldz/chat/service/`:
  - Extend `AbstractServiceIT`.
  - Test all CRUD operations.
  - Verify auditing timestamps and N+1 prevention.
- [ ] Implement `PersonalChatControllerIT` in `src/integration-test/java/pl/ldz/chat/controller/`:
  - Extend `AbstractControllerIT`.
  - Test RBAC and security constraints (authenticated/unauthenticated, roles).
  - Test validation errors and not-found cases.
- [ ] Run all tests and verify full project build with `mvn verify`.
