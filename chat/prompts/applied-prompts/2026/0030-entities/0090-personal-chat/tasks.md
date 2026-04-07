# Tasks for Implementing CRUD Functionality for PersonalChat

## Phase 1: Verification and Base Infrastructure
1. [x] Verify `pl.ldz.chat.entity.base.AbstractEntity` exists and contains `id`, `version`, `creationTimestamp`, and `updateTimestamp`.
2. [x] Verify `pl.ldz.chat.repository.base.AbstractRepository` exists and extends `JpaRepository` and `JpaSpecificationExecutor`.
3. [x] Verify `pl.ldz.chat.service.base.CrudService` exists and defines standard CRUD methods.
4. [x] Verify `pl.ldz.chat.exception.GlobalExceptionHandler` exists and handles `EntityNotFoundException` and `OptimisticLockException`.
5. [x] Verify `pl.ldz.chat.ITSharedPostgres` exists in the integration-test source set.
6. [x] Ensure JPA Auditing is enabled via `@EnableJpaAuditing` in a configuration class.

## Phase 1.5: Missing Base DTOs
7. [x] Create `UserResponseDto` in `pl.ldz.chat.dto` (fields: id, username, email, displayName, avatarUrl, online). [Already exists as a record]

## Phase 2: DTO and Mapper Implementation
8. [x] Create `PersonalChatRequestDto` in `pl.ldz.chat.dto` (fields: user1Id, user2Id) with `@NotNull` validation.
9. [x] Create `PersonalChatResponseDto` in `pl.ldz.chat.dto` (fields: id, user1 (UserResponseDto), user2 (UserResponseDto), version, creationTimestamp, updateTimestamp).
10. [x] Create `PersonalChatMapper` in `pl.ldz.chat.mapper` using MapStruct.
11. [x] Implement `toEntity` in `PersonalChatMapper` (ignoring ID and audit fields, handling User mapping).
12. [x] Implement `toResponseDto` in `PersonalChatMapper`.
13. [x] Implement `updateEntityFromDto` in `PersonalChatMapper` with `NullValuePropertyMappingStrategy.IGNORE`.
14. [x] Implement `toResponseDtoList` in `PersonalChatMapper`.

## Phase 3: Persistence and Service Layer
15. [x] Create `PersonalChatRepository` in `pl.ldz.chat.repository` extending `AbstractRepository<PersonalChat, UUID>`.
16. [x] Implement `@EntityGraph` or join fetches for `user1` and `user2` in `PersonalChatRepository` to prevent N+1 issues.
17. [x] Create `PersonalChatService` in `pl.ldz.chat.service` implementing `CrudService<PersonalChat, UUID, PersonalChatRequestDto, PersonalChatResponseDto>`.
18. [x] Implement `create` method in `PersonalChatService` (fetch User entities by ID before saving).
19. [x] Implement `getById`, `getAll` (paginated), `update`, and `delete` methods in `PersonalChatService` with appropriate `@Transactional`.

## Phase 4: API and Security
20. [x] Create `PersonalChatController` in `pl.ldz.chat.controller` with `@RestController` and `@RequestMapping("/api/v1/personal-chats")`.
21. [x] Implement `POST /` endpoint in `PersonalChatController` with `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")`.
22. [x] Implement `GET /{id}` endpoint in `PersonalChatController` with `@PreAuthorize("hasAnyRole('ADMIN')")` or participant check. [Simplified to hasRole('ADMIN') as per task list]
23. [x] Implement `GET /` (paginated) endpoint in `PersonalChatController` with `@PreAuthorize("hasRole('ADMIN')")` using `@ParameterObject Pageable`.
24. [x] Implement `PUT /{id}` endpoint in `PersonalChatController` with `@PreAuthorize("hasRole('ADMIN')")`.
25. [x] Implement `DELETE /{id}` endpoint in `PersonalChatController` with `@PreAuthorize("hasRole('ADMIN')")`.
26. [x] Add OpenAPI documentation (`@Tag`, `@Operation`, `@ApiResponse`) to all controller methods.
27. [x] Update `SecurityConfig` to ensure `/api/v1/personal-chats/**` is correctly secured.

## Phase 5: Testing and Validation
28. [x] Create `PersonalChatServiceIT` extending `AbstractServiceIT`.
29. [x] Test all CRUD operations in `PersonalChatServiceIT` and verify auditing timestamps and N+1 prevention.
30. [x] Create `PersonalChatControllerIT` extending `AbstractControllerIT`.
31. [x] Test RBAC and security constraints for all endpoints in `PersonalChatControllerIT`.
32. [x] Test validation errors (422) and not-found cases (404) in `PersonalChatControllerIT`.
33. [x] Run `mvn verify` to ensure all tests pass and the build is successful.
