# Implementation Plan for the Chat Project

This plan outlines the steps to improve and expand the chat project based on the guidelines in `prompts/requirements.md`.

## Phase 1: Core Messaging (CRUD for Message)
Implement full CRUD functionality for the `Message` entity.

### 1.1. DTOs
- Create `MessageRequestDto`: `senderId`, `chatRoomId` (optional), `personalChatId` (optional), `content`.
- Create `MessageResponseDto`: include all `AbstractEntity` fields + `sender` (UserResponseDto), `chatRoomId`, `personalChatId`, `content`, `edited`, `deleted`.
- Ensure no collections in `MessageResponseDto` yet.

### 1.2. Mapper
- Create `MessageMapper` (abstract class).
- Implement `idToUser`, `idToChatRoom`, `idToPersonalChat` using respective repositories.
- Map `senderId` to `sender`, `chatRoomId` to `chatRoom`, and `personalChatId` to `personalChat`.

### 1.3. Repository
- Create `MessageRepository` extending `AbstractRepository`.
- Apply `@EntityGraph(attributePaths = {"sender", "chatRoom", "personalChat"})` to `findById` and `findAll`.

### 1.4. Service
- Create `MessageService` implementing `CrudService`.
- Implement business logic (e.g., ensure either `chatRoomId` or `personalChatId` is present).

### 1.5. Controller
- Create `MessageController`.
- Secure endpoints: `hasAnyRole('USER', 'ADMIN')` for write, `authenticated()` for read (with potential future ownership checks).

### 1.6. Integration Tests
- Create `MessageServiceIT`.
- Create `MessageControllerIT`.

---

## Phase 2: Attachments (CRUD for Attachment)
Implement full CRUD functionality for the `Attachment` entity.

### 2.1. DTOs
- Create `AttachmentRequestDto`: `messageId`, `fileName`, `fileType`, `fileSize`, `url`.
- Create `AttachmentResponseDto`: include all `AbstractEntity` fields + `messageId`, `fileName`, `fileType`, `fileSize`, `url`.

### 2.2. Mapper
- Create `AttachmentMapper`.
- Map `messageId` to `message`.

### 2.3. Repository
- Create `AttachmentRepository`.
- Apply `@EntityGraph(attributePaths = {"message"})` to `findById` and `findAll`.

### 2.4. Service
- Create `AttachmentService`.

### 2.5. Controller
- Create `AttachmentController` (Refactor existing one if it doesn't match `requirements.md`).

### 2.6. Integration Tests
- Create `AttachmentServiceIT`.
- Create `AttachmentControllerIT`.

---

## Phase 3: Alignment and Refactoring
Ensure existing controllers and services follow the new standards.

### 3.1. User and ChatRoom Alignment
- Verify `UserRepository` and `ChatRoomRepository` have `@EntityGraph` for any collection fields exposed in DTOs.
- Ensure `UserController` and `ChatRoomController` use `@ParameterObject` for `Pageable`.

### 3.2. Security Configuration
- Update `SecurityConfig` to include new endpoints for `messages` and `attachments` with proper roles.

### 3.3. Validation and Error Handling
- Ensure all DTOs have proper Bean Validation.
- Verify `GlobalExceptionHandler` covers all edge cases introduced by new entities.
