# Project Improvement Plan: CRUD Implementation for ChatRoom

This plan outlines the steps for implementing full CRUD functionality for the `ChatRoom` entity, based on the `prompts/requirements.md` and following the project's `<guidelines>`.

## Phase 1: Verification and Base Infrastructure
1.  **Verify Base Classes**
    - Ensure `AbstractEntity` in `pl.ldz.chat.entity.base` is correctly implemented (manually implemented `equals` and `hashCode`, no `@Data`).
    - Ensure `AbstractRepository` in `pl.ldz.chat.repository.base` is available.
    - Ensure `CrudService` in `pl.ldz.chat.service.base` is available.
    - Verify `GlobalExceptionHandler` in `pl.ldz.chat.exception` handles all necessary exceptions (`EntityNotFoundException`, `OptimisticLockException`, etc.).

## Phase 2: DTO and Mapper Implementation
2.  **Create DTOs**
    - Implement `ChatRoomRequestDto` with Bean Validation.
    - Implement `ChatRoomResponseDto` including audit fields and version.
    - **Rule Check**: Ensure no collections in nested DTOs.
3.  **Implement MapStruct Mapper**
    - Create `ChatRoomMapper` in `pl.ldz.chat.mapper`.
    - Include mappings for `toEntity`, `toResponseDto`, and `updateEntityFromDto`.
    - Handle audit fields and ID ignoring during entity creation.

## Phase 3: Persistence and Service Layer
4.  **Implement Repository**
    - Create `ChatRoomRepository` in `pl.ldz.chat.repository`.
    - Extend `AbstractRepository`.
    - **N+1 Prevention**: Add `@EntityGraph` or join fetch methods for any collections if they are added later.
5.  **Implement Service**
    - Create `ChatRoomService` in `pl.ldz.chat.service`.
    - Implement methods: `create`, `getById`, `getAll` (paginated), `update`, `delete`.
    - Ensure `@Transactional` and `@Transactional(readOnly = true)` are correctly applied.

## Phase 4: API and Security
6.  **Implement Controller**
    - Create `ChatRoomController` in `pl.ldz.chat.controller`.
    - Define endpoints for POST, GET (by ID and paginated), PUT, and DELETE.
    - Apply RBAC using `@PreAuthorize`.
    - Add OpenAPI documentation (`@Operation`, `@ApiResponse`).
7.  **Security Configuration Verification**
    - Ensure roles (`VIEWER`, `USER`, `ADMIN`, `AUDITOR`) are correctly configured and recognized.

## Phase 5: Testing and Validation
8.  **Integration Testing (IT)**
    - Implement `ChatRoomServiceIT` in `src/integration-test/java`.
    - Implement `ChatRoomControllerIT` in `src/integration-test/java`.
    - Use Testcontainers (PostgreSQL) for both.
    - Verify all CRUD operations, role restrictions, and audit fields.
9.  **Unit Testing**
    - Implement `ChatRoomServiceTest` if there's complex business logic beyond simple CRUD.
10. **Final Verification**
    - Run `mvn verify` to ensure all tests pass and the build is stable.
