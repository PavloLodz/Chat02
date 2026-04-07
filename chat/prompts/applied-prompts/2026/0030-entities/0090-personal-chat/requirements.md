# CRUD Requirements for ${ENTITY_NAME}

This document defines the implementation steps for adding full CRUD functionality for a new entity in the `pl.ldz.chat` project.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = PersonalChat

### Derived class names
- `$REQUEST_DTO`              = ${ENTITY_NAME}RequestDto
- `$RESPONSE_DTO`             = ${ENTITY_NAME}ResponseDto
- `$REPOSITORY`               = ${ENTITY_NAME}Repository
- `$SERVICE`                  = ${ENTITY_NAME}Service
- `$CONTROLLER`               = ${ENTITY_NAME}Controller
- `$MAPPER`                   = ${ENTITY_NAME}Mapper

### Packages (Align with project structure)
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
- `$API_PATH`                 = /api/v1/personal-chats

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

## STEP 1 — Infrastructure Verification

1. Verify `AbstractEntity` exists in `${ENTITY_PACKAGE}.base`. All entities must extend it.
2. Verify `AbstractRepository` exists in `${REPO_BASE_PACKAGE}`.
3. Verify `CrudService` exists in `${SERVICE_BASE_PACKAGE}`.
4. Verify `GlobalExceptionHandler` exists in `${EXCEPTION_PACKAGE}` and handles `EntityNotFoundException` and `OptimisticLockException`.
5. Ensure `ITSharedPostgres` exists in `pl.ldz.chat` (integration-test source set) for shared Testcontainers configuration.

---

## STEP 2 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${REQUEST_DTO}` — fields matching all writable entity fields (exclude `id`, `version`, `creationTimestamp`, `updateTimestamp`). Use records or classes with Lombok.
2. `${RESPONSE_DTO}` — fields matching all entity fields including `id`, `version`, `creationTimestamp`, and `updateTimestamp`.
3. Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.) on the Request DTO.

**Constraints for REST API Mapping:**
- Only the root entity includes collection properties.
- Nested DTOs must NOT include collection-type fields to avoid deep recursion and large payloads.
- Avoid recursive or multi-level nesting of collections.

Use 2-space indentation.

---

## STEP 3 — Create Mapper

Create `${MAPPER}` in package `${MAPPER_PACKAGE}` using MapStruct:

```java
package ${MAPPER_PACKAGE};

import org.mapstruct.*;
import java.util.List;
import ${ENTITY_PACKAGE}.${ENTITY_NAME};
import ${DTO_PACKAGE}.${REQUEST_DTO};
import ${DTO_PACKAGE}.${RESPONSE_DTO};

@Mapper(componentModel = "spring")
public interface ${MAPPER} {

  ${RESPONSE_DTO} toResponseDto(${ENTITY_NAME} entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  ${ENTITY_NAME} toEntity(${REQUEST_DTO} requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  void updateEntityFromDto(${REQUEST_DTO} requestDto, @MappingTarget ${ENTITY_NAME} entity);

  List<${RESPONSE_DTO}> toResponseDtoList(List<${ENTITY_NAME}> entities);
}
```

---

## STEP 4 — Create Repository

Create `${REPOSITORY}` in package `${REPO_PACKAGE}` extending `AbstractRepository<${ENTITY_NAME}, ${ID_TYPE}>`:

**N+1 Prevention Rule:**
- Use `@EntityGraph` or join fetches for all collection-type fields or `@ManyToOne` relationships that are frequently accessed to avoid N+1 issues.

---

## STEP 5 — Create Service

Create `${SERVICE}` in package `${SERVICE_PACKAGE}` implementing `CrudService<${ENTITY_NAME}, ${ID_TYPE}, ${REQUEST_DTO}, ${RESPONSE_DTO}>`:

- Use `@Service` and `@Transactional`.
- Use `@Transactional(readOnly = true)` for read operations.
- Inject `${REPOSITORY}` and `${MAPPER}` via constructor.
- Throw custom `EntityNotFoundException` when an entity is not found.

---

## STEP 6 — Integration Tests (Service)

Create `${SERVICE}IT` in `src/integration-test/java/${SERVICE_PACKAGE_PATH}/` extending `AbstractServiceIT`:

- Test each CRUD method.
- Verify that `creationTimestamp` and `updateTimestamp` are handled correctly.
- Verify pagination logic.
- Use AssertJ for assertions.

---

## STEP 7 — Create Controller

Create `${CONTROLLER}` in package `${CONTROLLER_PACKAGE}`:

- Annotate with `@RestController`, `@RequestMapping("${API_PATH}")`.
- Inject `${SERVICE}` (or `CrudService` interface).
- Use `@PreAuthorize` for Role-Based Access Control (RBAC).
- Expose the following REST endpoints:
  - `POST /` — Create
  - `GET /{id}` — Get by ID
  - `GET /` — Get all (paginated)
  - `PUT /{id}` — Update
  - `DELETE /{id}` — Delete
- Use `@ParameterObject Pageable pageable` for pagination support.
- Add OpenAPI documentation (`@Tag`, `@Operation`, `@ApiResponse`).

---

## STEP 8 — Integration Tests (Controller)

Create `${CONTROLLER}IT` in `src/integration-test/java/${CONTROLLER_PACKAGE_PATH}/` extending `AbstractControllerIT`:

- Test each endpoint using `MockMvc`.
- Test security rules (authenticated vs unauthenticated, different roles).
- Test validation errors (422 Unprocessable Entity).
- Test not-found cases (404 Not Found).

---

## STEP 9 — Security Configuration

Update `SecurityConfig` in `${SECURITY_PACKAGE}` to permit/secure `${API_PATH}` endpoints according to the RBAC requirements.

---

## GENERAL RULES
- Use 2-space indentation.
- Follow Lombok usage rules (no `@Data` on JPA entities).
- All new code must follow the project structure and naming conventions.
- Ensure all tests (Unit and IT) pass before finalizing.
