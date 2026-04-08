# Project Improvement Plan

Based on the requirements defined in `prompts/requirements.md`, this plan outlines the steps to implement full CRUD functionality for the `RoomMember` entity and ensure the project follows consistent patterns.

## 1. Foundation & Infrastructure
- [ ] **Verify `AbstractEntity`**: Ensure `pl.ldz.chat.entity.base.AbstractEntity` includes all required fields (`id`, `version`, `creationTimestamp`, `updateTimestamp`) and correct auditing annotations.
- [ ] **Verify `AbstractRepository`**: Ensure `pl.ldz.chat.repository.base.AbstractRepository` correctly extends `JpaRepository` and `JpaSpecificationExecutor`.
- [ ] **Verify `CrudService`**: Ensure `pl.ldz.chat.service.base.CrudService` defines standard CRUD operations.

## 2. RoomMember CRUD Implementation
- [ ] **DTOs**: Create `RoomMemberRequestDto` and `RoomMemberResponseDto` in `pl.ldz.chat.dto`.
- [ ] **Mapper**: Create `RoomMemberMapper` in `pl.ldz.chat.mapper` using MapStruct.
- [ ] **Repository**: Create `RoomMemberRepository` in `pl.ldz.chat.repository` extending `AbstractRepository`.
- [ ] **Service**: Create `RoomMemberService` in `pl.ldz.chat.service` implementing `CrudService`.
- [ ] **Controller**: Create `RoomMemberController` in `pl.ldz.chat.controller` with REST endpoints and security annotations.

## 3. Testing & Validation
- [ ] **Repository Integration Tests**: Implement `RoomMemberRepositoryIT` to verify database operations and auditing.
- [ ] **Service Integration Tests**: Implement `RoomMemberServiceIT` to verify business logic and exception handling.
- [ ] **Controller Integration Tests**: Implement `RoomMemberControllerIT` to verify REST API contracts and security.

## 4. Global Enhancements
- [ ] **Global Exception Handling**: Ensure `GlobalExceptionHandler` handles `EntityNotFoundException` and validation errors consistently across all entities.
- [ ] **API Documentation**: Ensure all new endpoints are documented via SpringDoc OpenAPI.
