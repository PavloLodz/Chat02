# Task List: Message and Attachment CRUD Implementation

## Phase 1: Core Messaging (CRUD for Message)
- [x] 1.1. Create `MessageRequestDto` with `senderId`, `chatRoomId`, `personalChatId`, and `content` [x]
- [x] 1.2. Create `MessageResponseDto` including `AbstractEntity` fields, `sender` (UserResponseDto), `chatRoomId`, `personalChatId`, `content`, `edited`, and `deleted` [x]
- [x] 1.3. Create `MessageMapper` (abstract class) with `idToUser`, `idToChatRoom`, and `idToPersonalChat` mappings [x]
- [x] 1.4. Create `MessageRepository` extending `AbstractRepository` with `@EntityGraph` for `sender`, `chatRoom`, and `personalChat` [x]
- [x] 1.5. Create `MessageService` implementing `CrudService` with validation for `chatRoomId` or `personalChatId` presence [x]
- [x] 1.6. Create `MessageController` with RBAC security (`hasAnyRole('USER', 'ADMIN')`) [x]
- [x] 1.7. Create `MessageServiceIT` extending `AbstractServiceIT` [x]
- [x] 1.8. Create `MessageControllerIT` extending `AbstractControllerIT` [x]

## Phase 2: Attachments (CRUD for Attachment)
- [x] 2.1. Create `AttachmentRequestDto` with `messageId`, `fileName`, `fileType`, `fileSize`, and `url` [x]
- [x] 2.2. Create `AttachmentResponseDto` including `AbstractEntity` fields, `messageId`, `fileName`, `fileType`, `fileSize`, and `url` [x]
- [x] 2.3. Create `AttachmentMapper` mapping `messageId` to `message` [x]
- [x] 2.4. Create `AttachmentRepository` extending `AbstractRepository` with `@EntityGraph` for `message` [x]
- [x] 2.5. Create `AttachmentService` implementing `CrudService` [x]
- [x] 2.6. Create `AttachmentController` aligning with `requirements.md` [x]
- [x] 2.7. Create `AttachmentServiceIT` extending `AbstractServiceIT` [x]
- [x] 2.8. Create `AttachmentControllerIT` extending `AbstractControllerIT` [x]

## Phase 3: Alignment and Refactoring
- [x] 3.1. Verify `UserRepository` and `ChatRoomRepository` have `@EntityGraph` for collection fields exposed in DTOs [x]
- [x] 3.2. Update `UserController` and `ChatRoomController` to use `@ParameterObject` for `Pageable` [x]
- [x] 3.3. Update `SecurityConfig` to include new endpoints for `messages` and `attachments` with proper roles [x]
- [x] 3.4. Ensure all DTOs have proper Bean Validation (e.g., `@NotBlank`, `@NotNull`, `@Size`) [x]
- [x] 3.5. Verify `GlobalExceptionHandler` covers all edge cases introduced by new entities [x]
- [x] 3.6. Run full `mvn verify` to ensure project-wide consistency and passing tests [x]
