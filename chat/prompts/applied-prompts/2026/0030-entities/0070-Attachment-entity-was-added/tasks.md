# Task List: Attachment CRUD Implementation

## 1. Context and Prerequisites
- [x] Step 1.1: Ensure `Attachment` correctly extends `AbstractEntity` [x]
- [x] Step 1.2: Verify that audit fields and `id`/`version` are not re-declared in `Attachment` [x]
- [x] Step 1.3: Confirm `equals` and `hashCode` are properly handled in `Attachment` [x]

## 2. DTO Creation (`pl.ldz.chat.dto`)
- [x] Step 2.1: Create `AttachmentRequestDto` with validation annotations [x]
- [x] Step 2.2: Create `AttachmentResponseDto` including all fields [x]

## 3. Mapper Implementation (`pl.ldz.chat.mapper`)
- [x] Step 3.1: Create `AttachmentMapper` using MapStruct [x]
- [x] Step 3.2: Implement `toEntity`, `toResponseDto`, and `updateEntityFromDto` mappings [x]

## 4. Repository Implementation (`pl.ldz.chat.repository`)
- [x] Step 4.1: Create `AttachmentRepository` extending `AbstractRepository<Attachment, UUID>` [x]

## 5. Repository Integration Tests (`src/integration-test/java`)
- [x] Step 5.1: Create `AttachmentRepositoryIT` extending `AbstractRepositoryIT` [x]
- [x] Step 5.2: Test save, find, and audit field population in `AttachmentRepositoryIT` [x]

## 6. Service Implementation (`pl.ldz.chat.service`)
- [x] Step 6.1: Create `AttachmentService` implementing `CrudService` [x]
- [x] Step 6.2: Implement CRUD methods with `@Transactional` and `@RequiredArgsConstructor` [x]

## 7. Service Integration Tests (`src/integration-test/java`)
- [x] Step 7.1: Create `AttachmentServiceIT` extending `AbstractServiceIT` [x]
- [x] Step 7.2: Test business logic, mapping, and pagination in `AttachmentServiceIT` [x]

## 8. Controller Implementation (`pl.ldz.chat.controller`)
- [x] Step 8.1: Create `AttachmentController` with `@RestController` and `@RequestMapping("/api/v1/attachments")` [x]
- [x] Step 8.2: Implement CRUD endpoints (POST, GET, GET all, PUT, DELETE) [x]
- [x] Step 8.3: Add OpenAPI annotations and `@PreAuthorize` security [x]

## 9. Controller Integration Tests (`src/integration-test/java`)
- [x] Step 9.1: Create `AttachmentControllerIT` extending `AbstractControllerIT` [x]
- [x] Step 9.2: Test endpoints with `MockMvc` and `@WithMockUser` [x]

## 10. Final Polish and Global Exception Handling
- [x] Step 10.1: Verify `GlobalExceptionHandler` covers all cases for `Attachment` [x]
- [x] Step 10.2: Perform a full `mvn verify` to ensure everything works together [x]
