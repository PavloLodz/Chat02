# CRUD Requirements for ${ENTITY_NAME}

This document defines the implementation steps for adding full CRUD functionality for a new entity in the `pl.ldz.chat` project.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = Attachment

### Derived class names
- `$DTO`                      = ${ENTITY_NAME}Dto
- `$REPOSITORY`               = ${ENTITY_NAME}Repository
- `$SERVICE`                  = ${ENTITY_NAME}Service
- `$CONTROLLER`               = ${ENTITY_NAME}Controller
- `$MAPPER`                   = ${ENTITY_NAME}Mapper

### Packages (Align with Project Structure)
- `$BASE_PACKAGE`             = pl.ldz.chat
- `$ENTITY_PACKAGE`           = ${BASE_PACKAGE}.entity
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
- `$API_PATH`                 = /api/v1/attachments

---

## CONTEXT
Use the existing JPA entity class `${ENTITY_NAME}` (in package `${ENTITY_PACKAGE}`) as the source of truth for field names and types.
Apply the following steps IN ORDER. Each step must compile before proceeding to the next.

---

## STEP 0 — Dependencies (If missing)

Ensure the following dependencies are present in `pom.xml`:
- `spring-boot-starter-validation`
- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `org.mapstruct:mapstruct` and `org.mapstruct:mapstruct-processor`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `org.postgresql:postgresql`
- `org.testcontainers:postgresql` and `org.testcontainers:junit-jupiter` (scope: test)

---

## STEP 1 — Extend AbstractEntity

Ensure the entity extends `AbstractEntity` from `${ENTITY_PACKAGE}.base`.
`AbstractEntity` provides `id`, `version`, `creationTimestamp`, and `updateTimestamp`.

Do NOT re-declare these fields in `${ENTITY_NAME}`.

---

## STEP 2 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${ENTITY_NAME}RequestDto` — fields matching all writable entity fields (exclude `id`, `version`, `creationTimestamp`, `updateTimestamp`).
2. `${ENTITY_NAME}ResponseDto` — fields matching all entity fields including `id`, `version`, `creationTimestamp`, and `updateTimestamp`.
3. Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.) on the Request DTO.

Use 2-space indentation for all files.

---

## STEP 3 — Create Mapper

Create `${MAPPER}` in package `${MAPPER_PACKAGE}` using MapStruct:

```java
package ${MAPPER_PACKAGE};

import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ${MAPPER} {

  ${ENTITY_NAME}ResponseDto toResponseDto(${ENTITY_NAME} entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  ${ENTITY_NAME} toEntity(${ENTITY_NAME}RequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  void updateEntityFromDto(${ENTITY_NAME}RequestDto requestDto,
                           @MappingTarget ${ENTITY_NAME} entity);

  List<${ENTITY_NAME}ResponseDto> toResponseDtoList(List<${ENTITY_NAME}> entities);
}
```

---

## STEP 4 — Create Repository

Create `${REPOSITORY}` in package `${REPO_PACKAGE}` extending `AbstractRepository<${ENTITY_NAME}, ${ID_TYPE}>`:

```java
package ${REPO_PACKAGE};

import ${REPO_BASE_PACKAGE}.AbstractRepository;
import ${ENTITY_PACKAGE}.${ENTITY_NAME};
import java.util.UUID;

public interface ${REPOSITORY} extends AbstractRepository<${ENTITY_NAME}, ${ID_TYPE}> {
}
```

---

## STEP 5 — Repository Integration Tests

Create `${ENTITY_NAME}RepositoryIT` in `src/integration-test/java/${REPO_PACKAGE.replace('.', '/')}` extending `AbstractRepositoryIT`:

- Verify basic CRUD operations.
- Verify that `creationTimestamp` and `updateTimestamp` are automatically populated.
- Verify optimistic locking if necessary.

---

## STEP 6 — Create Service Implementation

Create `${SERVICE}` in package `${SERVICE_PACKAGE}` implementing `CrudService<${ENTITY_NAME}, ${ID_TYPE}, ${ENTITY_NAME}RequestDto, ${ENTITY_NAME}ResponseDto>`:

- Implement CRUD methods using `${REPOSITORY}` and `${MAPPER}`.
- Use `@Service` and `@Transactional`.
- Use `@Transactional(readOnly = true)` for read operations.
- Throw `EntityNotFoundException` (from `${EXCEPTION_PACKAGE}`) when not found.

---

## STEP 7 — Service Integration Tests

Create `${ENTITY_NAME}ServiceIT` in `src/integration-test/java/${SERVICE_PACKAGE.replace('.', '/')}` extending `AbstractServiceIT`:

- Test each CRUD method.
- Verify `EntityNotFoundException` is thrown when an entity is not found.
- Verify pagination.

---

## STEP 8 — Create Controller

Create `${CONTROLLER}` in package `${CONTROLLER_PACKAGE}`:

- Annotate with `@RestController`, `@RequestMapping("${API_PATH}")`.
- Inject `${SERVICE}`.
- Expose the following REST endpoints:

| Method | Path      | Request Body                | Response                              | Description          |
|--------|-----------|-----------------------------|---------------------------------------|----------------------|
| POST   | `/`       | `${ENTITY_NAME}RequestDto`  | `201 ${ENTITY_NAME}ResponseDto`       | Create entity        |
| GET    | `/{id}`   | —                           | `200 ${ENTITY_NAME}ResponseDto`       | Get by ID            |
| GET    | `/`       | —                           | `200 Page<${ENTITY_NAME}ResponseDto>` | Get all (paginated)  |
| PUT    | `/{id}`   | `${ENTITY_NAME}RequestDto`  | `200 ${ENTITY_NAME}ResponseDto`       | Update entity        |
| DELETE | `/{id}`   | —                           | `204 No Content`                      | Delete entity        |

- Use `@Valid` for request body parameters.
- Add OpenAPI documentation with `@Operation`, `@ApiResponse` on each method.
- Use `@PreAuthorize` for role-based access control.

---

## STEP 9 — Controller Integration Tests

Create `${ENTITY_NAME}ControllerIT` in `src/integration-test/java/${CONTROLLER_PACKAGE.replace('.', '/')}` extending `AbstractControllerIT`:

- Test each endpoint using `MockMvc`.
- Assert HTTP status codes and response JSON.
- Test validation errors (400 Bad Request).
- Test not-found cases (404 Not Found).
- Authenticate requests using `@WithMockUser`.

---

## STEP 10 — Global Error Handling

Ensure `GlobalExceptionHandler` in `${EXCEPTION_PACKAGE}` handles:
- `EntityNotFoundException`
- `OptimisticLockException`
- `MethodArgumentNotValidException` (Validation errors)

---

## GENERAL RULES
- Follow project guidelines.
- Use Java 21 features.
- Use 2-space indentation.
- Use constructor injection.
- Ensure all new files have correct package declarations and imports.
- Use MapStruct for ALL mappings.
- Use JUnit 5, AssertJ, and MockMvc for testing.
- Integration tests go to `src/integration-test/java` and end with `IT`.
- Unit tests go to `src/test/java` and end with `Test`.
