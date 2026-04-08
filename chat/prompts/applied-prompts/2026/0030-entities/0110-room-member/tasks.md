# Task List for RoomMember CRUD Implementation

Based on `prompts/plan.md` and `prompts/requirements.md`.

## 1. Foundation & Infrastructure
- [x] Verify `AbstractEntity` contains `id`, `version`, `creationTimestamp`, `updateTimestamp`. [x]
- [x] Verify `AbstractRepository` extends `JpaRepository` and `JpaSpecificationExecutor`. [x]
- [x] Verify `CrudService` defines standard CRUD operations (`create`, `getById`, `getAll`, `update`, `delete`). [x]

## 2. RoomMember CRUD Implementation
- [x] Create `RoomMemberRequestDto` in `pl.ldz.chat.dto`. [x]
- [x] Create `RoomMemberResponseDto` in `pl.ldz.chat.dto`. [x]
- [x] Create `RoomMemberMapper` in `pl.ldz.chat.mapper` with required `@Mapping` configurations. [x]
- [x] Create `RoomMemberRepository` in `pl.ldz.chat.repository` with `findByRoomId` query. [x]
- [x] Create `RoomMemberService` in `pl.ldz.chat.service` implementing `CrudService`. [x]
- [x] Create `RoomMemberController` in `pl.ldz.chat.controller` with REST endpoints and security. [x]

## 3. Testing & Validation
- [x] Create `RoomMemberRepositoryIT` in `src/integration-test/java/pl/ldz/chat/repository` to verify CRUD and auditing. [x]
- [x] Create `RoomMemberServiceIT` in `src/integration-test/java/pl/ldz/chat/service` to verify business logic and exceptions. [x]
- [x] Create `RoomMemberControllerIT` in `src/integration-test/java/pl/ldz/chat/controller` to verify endpoints and security. [x]

## 4. Global Enhancements
- [x] Ensure `GlobalExceptionHandler` handles `EntityNotFoundException`. [x]
- [x] Ensure `GlobalExceptionHandler` handles `OptimisticLockException` (if applicable). [x]
- [x] Ensure `GlobalExceptionHandler` handles validation errors (`MethodArgumentNotValidException`). [x]
- [x] Verify Swagger UI documentation for all new `RoomMember` endpoints. [x]
