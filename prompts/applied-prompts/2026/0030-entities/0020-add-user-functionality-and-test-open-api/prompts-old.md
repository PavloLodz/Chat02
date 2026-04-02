# Add functionality with User and 

## Variables

### Identity
- $ID_TYPE                  = UUID
- $ENTITY_NAME              = MyEntity

### Derived class names
- $DTO                      = ${ENTITY_NAME}Dto
- $REPOSITORY               = ${ENTITY_NAME}Repository
- $SERVICE                  = ${ENTITY_NAME}Service
- $CONTROLLER               = ${ENTITY_NAME}Controller
- $MAPPER                   = ${ENTITY_NAME}Mapper

### Packages
- $BASE_PACKAGE             = pl.ldz.chat
- $ENTITY_PACKAGE           = ${BASE_PACKAGE}.entity
- $DTO_PACKAGE              = ${BASE_PACKAGE}.dto
- $MAPPER_PACKAGE           = ${BASE_PACKAGE}.mapper
- $REPO_PACKAGE             = ${BASE_PACKAGE}.repository
- $REPO_BASE_PACKAGE        = ${BASE_PACKAGE}.repository.base
- $SERVICE_PACKAGE          = ${BASE_PACKAGE}.service
- $SERVICE_BASE_PACKAGE     = ${BASE_PACKAGE}.service.base
- $CONTROLLER_PACKAGE       = ${BASE_PACKAGE}.controller
- $SECURITY_PACKAGE         = ${BASE_PACKAGE}.config.security
- $EXCEPTION_PACKAGE        = ${BASE_PACKAGE}.exception

### URL
- $API_PATH                 = /api/v1/user

---

## CONTEXT
Use the existing JPA entity class ${ENTITY_NAME} (in package ${ENTITY_PACKAGE}) as the
source of truth for field names and types.
Apply the following steps IN ORDER. Each step must compile before proceeding to the next.

---

## STEP 1 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${ENTITY_NAME}RequestDto` — fields matching all writable entity fields
   (exclude `id`, `version`, audit fields).
2. `${ENTITY_NAME}ResponseDto` — fields matching all entity fields including `id` and `version`.
3. Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.) on the Request DTO.

---

## STEP 2 — Create Mapper

Create `${MAPPER}` in package `${MAPPER_PACKAGE}` using MapStruct:

```java
package ${MAPPER_PACKAGE};

@Mapper(componentModel = "spring")
public interface ${MAPPER} {

    ${ENTITY_NAME}ResponseDto toResponseDto(${ENTITY_NAME} entity);

    ${ENTITY_NAME} toEntity(${ENTITY_NAME}RequestDto requestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(${ENTITY_NAME}RequestDto requestDto,
                             @MappingTarget ${ENTITY_NAME} entity);

    List<${ENTITY_NAME}ResponseDto> toResponseDtoList(List<${ENTITY_NAME}> entities);
}
```

- Exclude `id` and `version` from `toEntity()` mapping using `@Mapping(target = "id", ignore = true)`
  and `@Mapping(target = "version", ignore = true)`.
- Ensure MapStruct is on the annotation processor classpath (`mapstruct-processor` in `pom.xml` / `build.gradle`).

---

## STEP 3 — Create Abstract Spring JPA CRUD Repository Interface with Pagination

Create `AbstractRepository` in package `${REPO_BASE_PACKAGE}`:

```java
package ${REPO_BASE_PACKAGE};

@NoRepositoryBean
public interface AbstractRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    Page<T> findAll(Pageable pageable);
}
```

---

## STEP 4 — Create Spring JPA Repository Interface

Create `${REPOSITORY}` in package `${REPO_PACKAGE}`:

```java
package ${REPO_PACKAGE};

public interface ${REPOSITORY} extends Abstract${REPOSITORY}<${ENTITY_NAME}, ${ID_TYPE}> {
    Optional<${ENTITY_NAME}> findByIdAndDeletedFalse(${ID_TYPE} id);
}
```

---

## STEP 5 — Create Abstract Integration Repository Test

Create `AbstractRepositoryTest` in `${REPO_PACKAGE}` (integration test sources):

- Annotate with `@DataJpaTest`
- Annotate with `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`
- Use `@Testcontainers` + PostgreSQL `@Container` for real DB
- Inject `${REPOSITORY}` via `@Autowired`
- Define abstract helper method: `protected abstract ${ENTITY_NAME} buildValidEntity();`
- Define shared test: `void save_andFindById_shouldReturnEntity()`

---

## STEP 6 — Create Integration Repository Test

Create `${REPOSITORY}IT` extending `Abstract${REPOSITORY}Test` in package `${REPO_PACKAGE}` (integration test sources):

- Implement `buildValidEntity()` with valid field values matching `${ENTITY_NAME}`
- Add at least 2 additional test methods specific to `${ENTITY_NAME}` queries

---

## STEP 7 — Create Abstract Service Class

Create `Service<T, ID, REQ, RES>`interface in package `${SERVICE_BASE_PACKAGE}`:

- Declare abstract methods:
    - `RES create(REQ request)`
    - `RES getById(ID id)`
    - `Page<RES> getAll(Pageable pageable)`
    - `RES update(ID id, REQ request)`
    - `void delete(ID id)`
- Inject the generic repository
- Throw `EntityNotFoundException` (from `${EXCEPTION_PACKAGE}`) when entity is not found

---

## STEP 8 — Create Service Class

Create `${SERVICE}` in package `${SERVICE_PACKAGE}` implementing the Service:

- Implement all CRUD methods using `${REPOSITORY}` and `${MAPPER}`
- Use `${MAPPER}` for all entity ↔ DTO conversions (no manual mapping)
- Use `updateEntityFromDto()` in the `update()` method
- Annotate with `@Service` and `@Transactional`
- Use `@Transactional(readOnly = true)` on read methods

---

## STEP 9 — Create Abstract Service Integration Test Class

Create `AbstractServiceIT` in `${SERVICE_PACKAGE}` (integration test sources):

- Annotate with `@SpringBootTest`
- Use `@Testcontainers` + PostgreSQL container
- Inject `${SERVICE}` via `@Autowired`
- Define abstract method: `protected abstract ${ENTITY_NAME}RequestDto buildValidRequest();`
- Define shared tests:
    - `create_shouldPersistAndReturnDto()`
    - `getById_whenNotExists_shouldThrowException()`
    - `getAll_shouldReturnPagedResults()`

---

## STEP 10 — Create Service Integration Test Class

Create `${SERVICE}IT` extending `Abstract${SERVICE}IT` in package `${SERVICE_PACKAGE}` (test sources):

- Implement `buildValidRequest()` with valid data for `${ENTITY_NAME}`
- Add at least 2 additional business-logic tests specific to `${ENTITY_NAME}`

---

## STEP 11 — Create Controller

Create `${CONTROLLER}` in package `${CONTROLLER_PACKAGE}`:

- Annotate with `@RestController`, `@RequestMapping("${API_PATH}")`
- Inject `${SERVICE}`
- Expose endpoints:
    - `POST   /`      → `create(@Valid @RequestBody ${ENTITY_NAME}RequestDto)`
    - `GET    /{id}`  → `getById(@PathVariable ${ID_TYPE} id)`
    - `GET    /`      → `getAll(Pageable pageable)`
    - `PUT    /{id}`  → `update(@PathVariable ${ID_TYPE} id, @Valid @RequestBody ${ENTITY_NAME}RequestDto)`
    - `DELETE /{id}`  → `delete(@PathVariable ${ID_TYPE} id)`
- Document each endpoint with OpenAPI annotations:
    - `@Operation(summary = "...")` on every method
    - `@ApiResponse(responseCode = "200", ...)`, `@ApiResponse(responseCode = "404", ...)`, etc.
    - `@Tag(name = "${ENTITY_NAME} Management")`
    - `@Parameter(description = "...")` on path/query params

---

## STEP 12 — Create Abstract Controller Integration Test Class

Create `AbstractControllerIT` in `${CONTROLLER_PACKAGE}` (integration test sources):

- Annotate with `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Annotate with `@AutoConfigureMockMvc`
- Use `@Testcontainers` + PostgreSQL container
- Inject `MockMvc`
- Define abstract method: `protected abstract T buildValidRequest();`
- Define shared tests for all 5 endpoints (happy path + 404)

---

## STEP 13 — Create Controller Integration Test Class

Create `${CONTROLLER}IT` extending `AbstractControllerIT` in package `${CONTROLLER_PACKAGE}` (test sources):

- Implement `buildValidRequest()`
- Add at least 2 additional controller-level tests

---

## STEP 14 — Create Abstract Security Configuration Class

Create `AbstractSecurityConfig` in package `${SECURITY_PACKAGE}`:

```java
package ${SECURITY_PACKAGE};

public abstract class AbstractSecurityConfig {

    protected SecurityFilterChain commonFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((req, res, ex) -> res.setStatus(HttpStatus.FORBIDDEN.value()))
            );
        return http.build();
    }
}
```

---

## STEP 15 — Create Security Configuration Class

Create `SecurityConfig` in package `${SECURITY_PACKAGE}` extending `AbstractSecurityConfig`:

- Annotate with `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`
- Define `SecurityFilterChain`:
    - `AUDIT` role: GET only on `${API_PATH}/**`
    - `USER` role: full CRUD on `${API_PATH}/**`
    - `ADMIN` role: full CRUD on `${API_PATH}/**`
    - All other requests → authenticated
- Apply `@PreAuthorize` on `${CONTROLLER}` methods:
    - `@PreAuthorize("hasAnyRole('USER','ADMIN')")` on `create`, `update`, `delete`
    - `@PreAuthorize("hasAnyRole('USER','ADMIN','AUDIT')")` on `getById`, `getAll`

---

## STEP 16 — Add Optimistic Locking

In the `${ENTITY_NAME}` JPA entity class (package `${ENTITY_PACKAGE}`):

- Add field:
  ```java
  @Version
  @Column(nullable = false)
  private Long version = 0L;
  ```
- Ensure `version` is included in `${ENTITY_NAME}ResponseDto`
- Ensure `update()` in `${SERVICE}` passes `version` via `updateEntityFromDto()` to prevent lost updates
- Create `GlobalExceptionHandler` in package `${EXCEPTION_PACKAGE}`:
    - Annotate with `@RestControllerAdvice`
    - Handle `OptimisticLockException` and `ObjectOptimisticLockingFailureException`
    - Return HTTP `409 Conflict` with a descriptive error message

---

## GENERAL RULES (apply to ALL steps)
- please follow these rules from `.junie/guidelines.md`
- Java 21+, Spring Boot 3.x, use `jakarta.*` not `javax.*`
- All classes must have proper `package` declarations matching the package variables above
- All new files must include necessary imports
- Use MapStruct for ALL entity ↔ DTO conversions — no manual mapping anywhere
- Test classes must use JUnit 5 (`@Test` from `org.junit.jupiter.api`)
- Use `Assertions.assertThat(...)` from AssertJ
- Testcontainers version must match the one in `pom.xml` / `build.gradle`
```

---
