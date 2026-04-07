# CRUD Requirements for ${ENTITY_NAME}

This document defines the implementation steps for adding full CRUD functionality for a new entity in the `pl.ldz.chat` project.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = Message

### Derived class names
- `$DTO`                      = ${ENTITY_NAME}Dto
- `$REPOSITORY`               = ${ENTITY_NAME}Repository
- `$SERVICE`                  = ${ENTITY_NAME}Service
- `$CONTROLLER`               = ${ENTITY_NAME}Controller
- `$MAPPER`                   = ${ENTITY_NAME}Mapper

### Packages (Align with .junie/guidelines.md)
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
- `$API_PATH`                 = /api/v1/messages

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

## STEP 1 — Create AbstractEntity

If it doesn't exist create `AbstractEntity` in package `${ENTITY_PACKAGE}`:

- This is the base class for **all** JPA entities in the project. Every entity must extend it.
- Annotate with `@MappedSuperclass` so JPA maps its fields into each subclass table.
- Annotate with `@EntityListeners(AuditingEntityListener.class)` to enable Spring Data auditing.
- Enable auditing globally by adding `@EnableJpaAuditing` to a `@Configuration` class in `${BASE_PACKAGE}.config` (create one if it does not exist).
- Declare the following fields:

| Field               | Type      | Annotations                                           | Notes                          |
|---------------------|-----------|-------------------------------------------------------|--------------------------------|
| `id`                | `UUID`    | `@Id`, `@GeneratedValue(strategy = AUTO)`             | Primary key                    |
| `version`           | `Long`    | `@Version`                                            | Optimistic locking             |
| `creationTimestamp` | `Instant` | `@CreatedDate`, `@Column(updatable = false)`          | Set once on insert, never changed |
| `updateTimestamp`   | `Instant` | `@LastModifiedDate`                                   | Updated on every merge         |

```java
package ${ENTITY_PACKAGE};

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Version
  private Long version;

  @CreatedDate
  @Column(updatable = false)
  private Instant creationTimestamp;

  @LastModifiedDate
  private Instant updateTimestamp;

  // getters only — no public setters for id, version, or audit fields
}
```

- Expose only getters for all four fields; do not add public setters for `id`, `version`, `creationTimestamp`, or `updateTimestamp`.
- Use 2-space indentation.

---

## STEP 2 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${ENTITY_NAME}RequestDto` — fields matching all writable entity fields (exclude `id`, `version`, `creationTimestamp`, `updateTimestamp`).
2. `${ENTITY_NAME}ResponseDto` — fields matching all entity fields including `id`, `version`, `creationTimestamp`, and `updateTimestamp`.
3. Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.) on the Request DTO.

Use 2-space indentation for all files.

---

## STEP 3 — Create Mapper

Mapper for the REST API:
    1. Only the root entity includes collection properties. 
    2. Nested DTOs must not include collection-type fields. 
    3. Avoid recursive or multi-level nesting of collections.
    4. Avoid N+1 during fetching collection type fields

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

## STEP 4 — Create Abstract Repository

If it doesn't exist create `AbstractRepository` in package `${REPO_BASE_PACKAGE}`:

```java
package ${REPO_BASE_PACKAGE};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.Serializable;

@NoRepositoryBean
public interface AbstractRepository<T, ID extends Serializable>
    extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
  Page<T> findAll(Pageable pageable);
}
```

---

## STEP 5 — Create Repository

Create `${REPOSITORY}` in package `${REPO_PACKAGE}`:

```java
package ${REPO_PACKAGE};

import ${REPO_BASE_PACKAGE}.AbstractRepository;
import ${ENTITY_PACKAGE}.${ENTITY_NAME};
import java.util.Optional;
import java.util.UUID;

public interface ${REPOSITORY} extends AbstractRepository<${ENTITY_NAME}, ${ID_TYPE}> {
  // Add domain specific queries if needed, e.g.,
  // Optional<${ENTITY_NAME}> findByIdAndDeletedFalse(${ID_TYPE} id);
}
```

---

## STEP 6 — Repository Integration Tests

If it doesn't exist create an abstract base class `AbstractRepositoryIntegrationTest` for repository tests using Testcontainers in `src/test/java/${REPO_BASE_PACKAGE}`:

- Use `@DataJpaTest` and `@AutoConfigureTestDatabase(replace = Replace.NONE)`.
- Use PostgreSQL container.
- Define shared test setup and verification:
    - Verify basic CRUD and custom queries.
    - Verify that `creationTimestamp` is non-null after save and does not change on update.
    - Verify that `updateTimestamp` changes after a subsequent save.

---

## STEP 7 — Create Service Interface

If it doesn't exist create `Service<T, ID, REQ, RES>` interface in package `${SERVICE_BASE_PACKAGE}`:

```java
package ${SERVICE_BASE_PACKAGE};

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Service<T, ID, REQ, RES> {

  RES create(REQ request);

  RES getById(ID id);

  Page<RES> getAll(Pageable pageable);

  RES update(ID id, REQ request);

  void delete(ID id);
}
```

---

## STEP 8 — Create Service Implementation

Create `${SERVICE}` in package `${SERVICE_PACKAGE}`:

- Implement CRUD methods using `${REPOSITORY}` and `${MAPPER}`.
- Use `@Service` and `@Transactional`.
- Use `@Transactional(readOnly = true)` for read operations.
- Throw custom `EntityNotFoundException` (from `${EXCEPTION_PACKAGE}`) when not found.

---

## STEP 9 — Service Integration Tests

Create an abstract base class `AbstractServiceIntegrationTest` in `src/test/java/${SERVICE_BASE_PACKAGE}/`:

```java
package ${SERVICE_BASE_PACKAGE};

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
public abstract class AbstractServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}
```

Create `${SERVICE}IntegrationTest` extending `AbstractServiceIntegrationTest` in `src/test/java/${SERVICE_PACKAGE}/`:

- Inject `${SERVICE}` via `@Autowired`.
- Test each CRUD method with valid and invalid inputs.
- Verify `EntityNotFoundException` is thrown when an entity is not found (use `assertThatThrownBy`).
- Verify pagination: call `getAll(PageRequest.of(0, 5))` and assert correct page size and total elements.
- Verify that `creationTimestamp` is non-null after `create()` and does not change after `update()`.
- Verify that `updateTimestamp` is non-null after `create()` and changes after `update()`.
- All tests annotated with `@Test` from JUnit 5.
- Use AssertJ assertions (`assertThat`, `assertThatThrownBy`).

---

## STEP 10 — Create Controller

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

- Pagination support on `GET /`:
  - Accept `Pageable` as a method parameter (resolved automatically by Spring's `PageableHandlerMethodArgumentResolver`).
  - Support query params: `?page=0&size=20&sort=fieldName,asc`.
  - Enable `PageableHandlerMethodArgumentResolver` in a `@Configuration` class in `${BASE_PACKAGE}.config` if not already registered.
  - Return `Page<${ENTITY_NAME}ResponseDto>` — Spring will serialize it to JSON including `content`, `totalElements`, `totalPages`, `number`, `size`.

```java
@GetMapping
@Operation(summary = "Get all ${ENTITY_NAME}s (paginated)")
@ApiResponse(responseCode = "200", description = "Page of ${ENTITY_NAME}s")
@PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
public ResponseEntity<Page<${ENTITY_NAME}ResponseDto>> getAll(
    @ParameterObject Pageable pageable) {
  return ResponseEntity.ok(${SERVICE_FIELD}.getAll(pageable));
}
```

  - Add `org.springdoc:springdoc-openapi-starter-webmvc-ui` dependency to expose pageable params in Swagger UI via `@ParameterObject`.

- Use `@Valid` for request body parameters.
- Add OpenAPI documentation with `@Operation`, `@ApiResponse` on each method.

---

## STEP 11 — Controller Integration Tests

If it doesn't exist create an abstract base class `AbstractControllerIntegrationTest` in `src/test/java/${CONTROLLER_PACKAGE}/`:

```java
package ${CONTROLLER_PACKAGE};

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractControllerIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
}
```

Create `${CONTROLLER}IntegrationTest` extending `AbstractControllerIntegrationTest` in `src/test/java/${CONTROLLER_PACKAGE}/`:

- Test each endpoint using `MockMvc` (`perform`, `andExpect`).
- Use `objectMapper.writeValueAsString(...)` to serialize request bodies.
- Assert HTTP status codes, response JSON fields, and pagination metadata.
- Assert that `creationTimestamp` and `updateTimestamp` are present and non-null in all responses.
- Test validation errors: send invalid request bodies and assert `400 Bad Request`.
- Test not-found cases: use a random UUID and assert `404 Not Found`.
- Test pagination on `GET /`: assert `$.content` is an array, `$.totalElements`, `$.totalPages`, `$.size`.
- Secure endpoints — authenticate test requests using `@WithMockUser(roles = "ADMIN")` (or equivalent) where required.
- All tests annotated with `@Test` from JUnit 5.
- Use AssertJ assertions and MockMvc result matchers (`status()`, `jsonPath()`).

---

## STEP 12 — Security Configuration

Create security configuration in `${SECURITY_PACKAGE}`:

- Define `SecurityFilterChain`.
- Use role-based access control (RBAC):
  - `VIEWER`, `USER`, `ADMIN`, `AUDITOR`.
- Secure endpoints based on roles:
  - `USER` and `ADMIN` for write operations.
  - `VIEWER`, `USER`, `ADMIN`, `AUDITOR` for read operations.
- Apply `@PreAuthorize` on controller methods.

---

## STEP 13 — Optimistic Locking and Global Error Handling

1. Confirm `@Version`, `creationTimestamp`, and `updateTimestamp` are inherited from `AbstractEntity` — do not re-declare them on `${ENTITY_NAME}`.
2. Confirm `version`, `creationTimestamp`, and `updateTimestamp` are included in `${ENTITY_NAME}ResponseDto` and mapped correctly in STEP 3.
3. Create `GlobalExceptionHandler` in `${EXCEPTION_PACKAGE}` using `@RestControllerAdvice`.
4. Handle `EntityNotFoundException`, `OptimisticLockException`, and validation errors.
5. Return consistent error responses.

---

## GENERAL RULES
- Follow `.junie/guidelines.md`.
- Use Java 21 features.
- Use 2-space indentation.
- Use constructor injection.
- Ensure all new files have correct package declarations and imports.
- Use MapStruct for ALL mappings.
- Use JUnit 5 and AssertJ for testing.
