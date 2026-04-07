# Task List: ChatRoom CRUD Implementation

This task list follows the implementation plan in `prompts/plan.md` and fulfills the requirements in `prompts/requirements.md`.

## Phase 1: Verification and Base Infrastructure
- [x] 1.1. Verify `AbstractEntity` in `pl.ldz.chat.entity.base` (manually implemented `equals` and `hashCode`, no `@Data`).
- [x] 1.2. Verify `AbstractRepository` in `pl.ldz.chat.repository.base` exists.
- [x] 1.3. Verify `CrudService` in `pl.ldz.chat.service.base` exists.
- [x] 1.4. Verify `GlobalExceptionHandler` in `pl.ldz.chat.exception` handles `EntityNotFoundException`, `OptimisticLockException`, and validation errors.

## Phase 2: DTO and Mapper Implementation
- [x] 2.1. Create `ChatRoomRequestDto` in `pl.ldz.chat.dto` with Bean Validation annotations.
- [x] 2.2. Create `ChatRoomResponseDto` in `pl.ldz.chat.dto` including audit fields and version.
- [x] 2.3. Verify no collection-type fields are present in nested DTOs (if any).
- [x] 2.4. Create `ChatRoomMapper` in `pl.ldz.chat.mapper` using MapStruct.
- [x] 2.5. Implement `toEntity`, `toResponseDto`, `updateEntityFromDto`, and `toResponseDtoList` in the mapper.

## Phase 3: Persistence and Service Layer
- [x] 3.1. Create `ChatRoomRepository` in `pl.ldz.chat.repository` extending `AbstractRepository`.
- [x] 3.2. Ensure N+1 prevention in the repository (e.g., using `@EntityGraph` for any collections).
- [x] 3.3. Create `ChatRoomService` in `pl.ldz.chat.service`.
- [x] 3.4. Implement `create`, `getById`, `getAll` (paginated), `update`, and `delete` in the service.
- [x] 3.5. Ensure correct use of `@Transactional` (read-only for GET operations).

## Phase 4: API and Security
- [x] 4.1. Create `ChatRoomController` in `pl.ldz.chat.controller`.
- [x] 4.2. Implement endpoints: POST `/`, GET `/{id}`, GET `/` (paginated), PUT `/{id}`, DELETE `/{id}`.
- [x] 4.3. Apply RBAC using `@PreAuthorize` on controller methods as specified.
- [x] 4.4. Add OpenAPI documentation (`@Operation`, `@ApiResponse`) to all endpoints.
- [x] 4.5. Verify security configuration recognizes roles: `VIEWER`, `USER`, `ADMIN`, `AUDITOR`.

## Phase 5: Testing and Validation
- [x] 5.1. Implement `ChatRoomServiceIT` in `src/integration-test/java` using PostgreSQL Testcontainers.
- [x] 5.2. Implement `ChatRoomControllerIT` in `src/integration-test/java` using PostgreSQL Testcontainers and MockMvc.
- [x] 5.3. Verify successful CRUD, validation errors, and role-based access in integration tests.
- [x] 5.4. Verify audit fields (`creationTimestamp`, `updateTimestamp`) in integration tests.
- [ ] 5.5. Implement `ChatRoomServiceTest` in `src/test/java` if complex logic is present.
- [x] 5.6. Run `mvn verify` and ensure all tests pass.
