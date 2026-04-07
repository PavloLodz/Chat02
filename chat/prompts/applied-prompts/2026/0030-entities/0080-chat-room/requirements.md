# CRUD Requirements for ${ENTITY_NAME}

This document defines the implementation steps for adding full CRUD functionality for a new entity in the `pl.ldz.chat` project.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = ChatRoom

### Derived class names
- `$DTO_REQUEST`              = ${ENTITY_NAME}RequestDto
- `$DTO_RESPONSE`             = ${ENTITY_NAME}ResponseDto
- `$REPOSITORY`               = ${ENTITY_NAME}Repository
- `$SERVICE`                  = ${ENTITY_NAME}Service
- `$CONTROLLER`               = ${ENTITY_NAME}Controller
- `$MAPPER`                   = ${ENTITY_NAME}Mapper

### Packages (Align with project structure)
- `$BASE_PACKAGE`             = pl.ldz.chat
- `$ENTITY_PACKAGE`           = ${BASE_PACKAGE}.entity
- `$ENTITY_BASE_PACKAGE`      = ${BASE_PACKAGE}.entity.base
- `$DTO_PACKAGE`              = ${BASE_PACKAGE}.dto
- `$MAPPER_PACKAGE`           = ${BASE_PACKAGE}.mapper
- `$REPO_PACKAGE`             = ${BASE_PACKAGE}.repository
- `$REPO_BASE_PACKAGE`        = ${BASE_PACKAGE}.repository.base
- `$SERVICE_PACKAGE`          = ${BASE_PACKAGE}.service
- `$SERVICE_BASE_PACKAGE`     = ${BASE_PACKAGE}.service.base
- `$CONTROLLER_PACKAGE`       = ${BASE_PACKAGE}.controller
- `$SECURITY_PACKAGE`         = ${BASE_PACKAGE}.security
- `$EXCEPTION_PACKAGE`        = ${BASE_PACKAGE}.exception

### URL
- `$API_PATH`                 = /api/v1/chat-rooms

---

## CONTEXT
Use the existing JPA entity class `${ENTITY_NAME}` (in package `${ENTITY_PACKAGE}`) as the source of truth for field names and types.
Apply the following steps IN ORDER. Each step must compile before proceeding to the next.

---

## STEP 1 — Verify Base Classes

Ensure the following base classes exist and are correctly implemented:
1. `AbstractEntity` in `${ENTITY_BASE_PACKAGE}`:
    - Provides `id`, `version`, `creationTimestamp`, `updateTimestamp`.
    - Uses `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`.
    - Manually implements `equals()` and `hashCode()`.
2. `AbstractRepository` in `${REPO_BASE_PACKAGE}`:
    - Extends `JpaRepository<T, ID>` and `JpaSpecificationExecutor<T>`.
3. `CrudService` in `${SERVICE_BASE_PACKAGE}`:
    - Defines standard CRUD methods.
4. `GlobalExceptionHandler` in `${EXCEPTION_PACKAGE}`:
    - Handles `EntityNotFoundException`, `OptimisticLockException`, `MethodArgumentNotValidException`, and JWT-related exceptions.

---

## STEP 2 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${DTO_REQUEST}` — fields matching all writable entity fields.
    - Exclude `id`, `version`, `creationTimestamp`, `updateTimestamp`.
    - Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.).
2. `${DTO_RESPONSE}` — fields matching all entity fields including `id`, `version`, `creationTimestamp`, and `updateTimestamp`.

**Rules for Collections and Nesting:**
- Only the root entity in a response should include collection properties.
- Nested DTOs MUST NOT include collection-type fields to avoid deep recursion and large payloads.
- Avoid recursive or multi-level nesting of collections.

---

## STEP 3 — Create Mapper

Create `${MAPPER}` in package `${MAPPER_PACKAGE}` using MapStruct:

```java
package ${MAPPER_PACKAGE};

import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ${MAPPER} {

  ${DTO_RESPONSE} toResponseDto(${ENTITY_NAME} entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  ${ENTITY_NAME} toEntity(${DTO_REQUEST} requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  void updateEntityFromDto(${DTO_REQUEST} requestDto, @MappingTarget ${ENTITY_NAME} entity);

  List<${DTO_RESPONSE}> toResponseDtoList(List<${ENTITY_NAME}> entities);
}
```

---

## STEP 4 — Create Repository

Create `${REPOSITORY}` in package `${REPO_PACKAGE}`:

```java
package ${REPO_PACKAGE};

import ${REPO_BASE_PACKAGE}.AbstractRepository;
import ${ENTITY_PACKAGE}.${ENTITY_NAME};
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.UUID;

public interface ${REPOSITORY} extends AbstractRepository<${ENTITY_NAME}, ${ID_TYPE}> {
  
  // Use @EntityGraph or join fetch to avoid N+1 issues when fetching collections
  // @EntityGraph(attributePaths = {"someCollection"})
  // Optional<${ENTITY_NAME}> findWithCollectionsById(UUID id);
}
```

---

## STEP 5 — Create Service

Create `${SERVICE}` in package `${SERVICE_PACKAGE}`:

- Implement `${SERVICE}` interface (if specific) or extend an abstract base if available.
- Implement CRUD methods using `${REPOSITORY}` and `${MAPPER}`.
- Use `@Service` and `@Transactional`.
- Use `@Transactional(readOnly = true)` for read operations.
- Throw `EntityNotFoundException` (from `${EXCEPTION_PACKAGE}`) when not found.
- **N+1 Prevention:** Ensure that when fetching entities with collections for the response, you use repository methods that fetch them efficiently.

---

## STEP 6 — Create Controller

Create `${CONTROLLER}` in package `${CONTROLLER_PACKAGE}`:

- Annotate with `@RestController`, `@RequestMapping("${API_PATH}")`.
- Inject `${SERVICE}`.
- Use `@Valid` for request body parameters.
- Add OpenAPI documentation with `@Operation`, `@ApiResponse` on each method.
- Use `@PreAuthorize` for role-based access control.

| Method | Path      | Body            | Response                  | Roles                                     |
|--------|-----------|-----------------|---------------------------|-------------------------------------------|
| POST   | `/`       | `${DTO_REQUEST}`| `201 ${DTO_RESPONSE}`     | `USER`, `ADMIN`                           |
| GET    | `/{id}`   | —               | `200 ${DTO_RESPONSE}`     | `VIEWER`, `USER`, `ADMIN`, `AUDITOR`     |
| GET    | `/`       | —               | `200 Page<${DTO_RESPONSE}>`| `VIEWER`, `USER`, `ADMIN`, `AUDITOR`     |
| PUT    | `/{id}`   | `${DTO_REQUEST}`| `200 ${DTO_RESPONSE}`     | `USER`, `ADMIN`                           |
| DELETE | `/{id}`   | —               | `204 No Content`          | `ADMIN`                                   |

---

## STEP 7 — Testing

### Integration Tests (IT)
- Create `${SERVICE}IT` and `${CONTROLLER}IT` in `src/integration-test/java`.
- Suffix integration tests with `IT`.
- Use abstract base classes (e.g., `AbstractControllerIT`, `AbstractServiceIT`) for shared setup (PostgreSQL Testcontainers).
- Verify:
    - Successful CRUD operations.
    - Validation errors (400/422).
    - Entity not found (404).
    - Security/Role restrictions (401/403).
    - Optimistic locking (if applicable).
    - Audit fields (`creationTimestamp`, `updateTimestamp`).

### Unit Tests
- Create `${SERVICE}Test` in `src/test/java` for complex business logic.
- Use Mockito to mock repository and mapper.

---

## GENERAL RULES
- Follow all guidelines in `.junie/guidelines.md`.
- Use Java 21 features.
- Use **2-space indentation** for Java and XML.
- Use constructor injection (`@RequiredArgsConstructor`).
- Use Lombok (`@Getter`, `@Setter`, `@Builder`, `@Slf4j`).
- **NO `@Data` on JPA entities.**
- All new methods and classes should be well-named and focused.
