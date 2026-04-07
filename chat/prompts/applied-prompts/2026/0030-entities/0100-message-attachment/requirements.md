# CRUD Requirements for ${ENTITY_NAME}

This document defines the implementation steps for adding full CRUD functionality for a new entity in the `pl.ldz.chat` project.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = [EntityName] (e.g., Message, Attachment)

### Derived class names
- `$DTO_REQUEST`              = ${ENTITY_NAME}RequestDto
- `$DTO_RESPONSE`             = ${ENTITY_NAME}ResponseDto
- `$REPOSITORY`               = ${ENTITY_NAME}Repository
- `$SERVICE`                  = ${ENTITY_NAME}Service
- `$CONTROLLER`               = ${ENTITY_NAME}Controller
- `$MAPPER`                   = ${ENTITY_NAME}Mapper

### Packages
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
- `$API_PATH`                 = /api/v1/[resource-plural] (e.g., /api/v1/messages)

---

## CONTEXT
Use the existing JPA entity class `${ENTITY_NAME}` (in package `${ENTITY_PACKAGE}`) as the source of truth for field names and types.
All entities MUST extend `${ENTITY_BASE_PACKAGE}.AbstractEntity`.

---

## STEP 1 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `${DTO_REQUEST}` — fields matching all writable entity fields.
    - Exclude `id`, `version`, `creationTimestamp`, `updateTimestamp`.
    - Use primitive IDs (e.g., `UUID userId`) instead of nested DTOs for input.
    - Add Bean Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, etc.).
2. `${DTO_RESPONSE}` — fields matching all entity fields.
    - Include `id`, `version`, `creationTimestamp`, and `updateTimestamp`.
    - Use nested DTOs for relationships (e.g., `UserResponseDto user`).

**Rules for DTOs:**
- Only the root entity in a response should include collection properties.
- Nested DTOs MUST NOT include collection-type fields to avoid deep recursion and large payloads.
- Avoid recursive or multi-level nesting of collections.

---

## STEP 2 — Create Mapper

Create `${MAPPER}` in package `${MAPPER_PACKAGE}` using MapStruct. 
Use an `abstract class` if you need to inject other repositories to resolve IDs to entities.

```java
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class ${MAPPER} {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  // Add mappings for relationships if IDs are used in RequestDto
  // @Mapping(target = "user", source = "userId", qualifiedByName = "idToUser")
  public abstract ${ENTITY_NAME} toEntity(${DTO_REQUEST} requestDto);

  public abstract ${DTO_RESPONSE} toResponseDto(${ENTITY_NAME} entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  public abstract void updateEntityFromDto(${DTO_REQUEST} requestDto, @MappingTarget ${ENTITY_NAME} entity);

  public abstract List<${DTO_RESPONSE}> toResponseDtoList(List<${ENTITY_NAME}> entities);
}
```

---

## STEP 3 — Create Repository

Create `${REPOSITORY}` in package `${REPO_PACKAGE}`. 
Extend `${REPO_BASE_PACKAGE}.AbstractRepository<${ENTITY_NAME}, UUID>`.

**N+1 Prevention:**
- Use `@EntityGraph` to eager load relationships in `findById` and `findAll` (and any other query returning collections).

```java
public interface ${REPOSITORY} extends AbstractRepository<${ENTITY_NAME}, UUID> {

  @Override
  @EntityGraph(attributePaths = {"relationship1", "relationship2"})
  Optional<${ENTITY_NAME}> findById(UUID id);

  @Override
  @EntityGraph(attributePaths = {"relationship1", "relationship2"})
  Page<${ENTITY_NAME}> findAll(Pageable pageable);
}
```

---

## STEP 4 — Create Service

Create `${SERVICE}` in package `${SERVICE_PACKAGE}`.
Implement `${SERVICE_BASE_PACKAGE}.CrudService<${ENTITY_NAME}, UUID, ${DTO_REQUEST}, ${DTO_RESPONSE}>`.

- Use `@Service` and `@RequiredArgsConstructor`.
- Use `@Transactional` on the class or write methods.
- Use `@Transactional(readOnly = true)` for read operations.
- Throw `${EXCEPTION_PACKAGE}.EntityNotFoundException` when an entity is not found.

---

## STEP 5 — Create Controller

Create `${CONTROLLER}` in package `${CONTROLLER_PACKAGE}`.

- Annotate with `@RestController`, `@RequestMapping("${API_PATH}")`, `@RequiredArgsConstructor`, and `@Tag`.
- Inject `${SERVICE}`.
- Expose standard CRUD endpoints:
    - `POST /` (Return 201 Created)
    - `GET /{id}`
    - `GET /` (Paginated, use `@ParameterObject Pageable pageable`)
    - `PUT /{id}`
    - `DELETE /{id}` (Return 204 No Content)
- Use `@Valid` for request bodies.
- Apply `@PreAuthorize` for RBAC security:
    - Typically `hasRole('ADMIN')` for sensitive operations or generic access.
    - Use `hasAnyRole('USER', 'ADMIN')` for common user operations.

---

## STEP 6 — Integration Tests

Create integration tests in `src/integration-test/java/`.

1. **Service IT**: `${SERVICE}IT` extending `pl.ldz.chat.service.base.AbstractServiceIT`.
    - Test all CRUD operations.
    - Verify auditing fields (`creationTimestamp`, `updateTimestamp`) and `@Version`.
2. **Controller IT**: `${CONTROLLER}IT` extending `pl.ldz.chat.controller.AbstractControllerIT`.
    - Test all REST endpoints using `MockMvc`.
    - Verify security (RBAC) and validation.
    - Verify pagination metadata in `GET /` response.

**Note:** All integration tests should indirectly extend `pl.ldz.chat.ITSharedPostgres` (via the abstract base classes) to use a shared PostgreSQL container.

---

## GENERAL RULES
- Use 2-space indentation.
- Use Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`).
- Follow the project's package structure strictly.
- Ensure N+1 issues are addressed in the repository layer using `@EntityGraph`.
- Ensure DTO collection rules are followed (only root level).
