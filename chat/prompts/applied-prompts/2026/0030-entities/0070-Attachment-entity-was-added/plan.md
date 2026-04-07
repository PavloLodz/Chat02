# Plan for Implementation of CRUD for Attachment

Based on the finalized requirements in `prompts/requirements.md`, this plan details the steps to implement full CRUD functionality for the `Attachment` entity.

## 1. Context and Prerequisites
- **Entity**: `Attachment` (already exists in `pl.ldz.chat.entity`).
- **Base Components**: `AbstractEntity`, `CrudService`, `AbstractRepositoryIT`, `AbstractServiceIT`, and `AbstractControllerIT` are already implemented.
- **Goal**: Follow the 10-step process defined in `requirements.md` to achieve a fully tested REST API for `Attachment`.

## 2. Implementation Steps

### Step 1: Verification of `Attachment` Entity
- [ ] Ensure `Attachment` correctly extends `AbstractEntity`.
- [ ] Verify that audit fields and `id`/`version` are not re-declared.
- [ ] Confirm `equals` and `hashCode` are properly handled (inherited or manually implemented as per guidelines).

### Step 2: DTO Creation (`pl.ldz.chat.dto`)
- [ ] Create `AttachmentRequestDto`:
  - Fields: `messageId` (UUID), `fileName` (String), `fileType` (String), `fileSize` (Long), `url` (String).
  - Add validation: `@NotNull` for all, `@NotBlank` for strings, `@Min(0)` for `fileSize`.
- [ ] Create `AttachmentResponseDto`:
  - Include all writable fields + `id`, `version`, `creationTimestamp`, `updateTimestamp`.

### Step 3: Mapper Implementation (`pl.ldz.chat.mapper`)
- [ ] Create `AttachmentMapper` using MapStruct.
- [ ] Handle `Message` <-> `UUID` conversion if necessary, or simply map fields.
- [ ] Ensure standard mappings for `toEntity`, `toResponseDto`, and `updateEntityFromDto`.

### Step 4: Repository Implementation (`pl.ldz.chat.repository`)
- [ ] Create `AttachmentRepository` extending `AbstractRepository<Attachment, UUID>`.

### Step 5: Repository Integration Tests (`src/integration-test/java`)
- [ ] Create `AttachmentRepositoryIT` extending `AbstractRepositoryIT`.
- [ ] Test save, find, and audit field population.

### Step 6: Service Implementation (`pl.ldz.chat.service`)
- [ ] Create `AttachmentService` implementing `CrudService<Attachment, UUID, AttachmentRequestDto, AttachmentResponseDto>`.
- [ ] Use `@Transactional` and `@RequiredArgsConstructor`.
- [ ] Implement `EntityNotFoundException` handling.

### Step 7: Service Integration Tests (`src/integration-test/java`)
- [ ] Create `AttachmentServiceIT` extending `AbstractServiceIT`.
- [ ] Test business logic and mapping.

### Step 8: Controller Implementation (`pl.ldz.chat.controller`)
- [ ] Create `AttachmentController` with `@RestController` and `@RequestMapping("/api/v1/attachments")`.
- [ ] Implement all CRUD endpoints (POST, GET, GET all, PUT, DELETE).
- [ ] Add OpenAPI annotations.
- [ ] Secure with `@PreAuthorize`.

### Step 9: Controller Integration Tests (`src/integration-test/java`)
- [ ] Create `AttachmentControllerIT` extending `AbstractControllerIT`.
- [ ] Test endpoints with `MockMvc` and `@WithMockUser`.

### Step 10: Final Polish and Global Exception Handling
- [ ] Verify `GlobalExceptionHandler` covers all cases for `Attachment`.
- [ ] Perform a full `mvn verify` to ensure everything works together.

## 3. Verification Strategy
- **Unit Tests**: Ensure mappers and DTO validations work.
- **Integration Tests**: 
  - Repository level (DB interactions).
  - Service level (Business logic).
  - Controller level (API contracts and Security).
- **Manual Verification**: Use Swagger UI (`/swagger-ui.html`) to test endpoints if possible.
