# CRUD Requirements for RoomMember

This document defines the implementation steps for adding full CRUD functionality for the `RoomMember` entity in the `pl.ldz.chat` project. It serves as a template for other entities as well.

## Variables

### Identity
- `$ID_TYPE`                  = UUID
- `$ENTITY_NAME`              = RoomMember

### Derived class names
- `$DTO_REQ`                  = RoomMemberRequestDto
- `$DTO_RES`                  = RoomMemberResponseDto
- `$REPOSITORY`               = RoomMemberRepository
- `$SERVICE`                  = RoomMemberService
- `$CONTROLLER`               = RoomMemberController
- `$MAPPER`                   = RoomMemberMapper

### Packages
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
- `$API_PATH`                 = /api/v1/room-members

---

## CONTEXT
Use the existing JPA entity class `RoomMember` (in package `${ENTITY_PACKAGE}`) as the source of truth for field names and types.
Apply the following steps IN ORDER. Each step must compile before proceeding to the next.

---

## STEP 1 — Verify/Update AbstractEntity

Ensure `AbstractEntity` in package `pl.ldz.chat.entity.base` is correctly implemented:

- Extends `@MappedSuperclass` and uses `@EntityListeners(AuditingEntityListener.class)`.
- Fields: `id` (UUID), `version` (Long), `creationTimestamp` (Instant), `updateTimestamp` (Instant).
- Annotations: `@Id`, `@Version`, `@CreatedDate`, `@LastModifiedDate`.
- Auditing enabled via `@EnableJpaAuditing` in a configuration class (e.g., `JpaConfig`).

---

## STEP 2 — Create DTOs

Create the following classes in package `${DTO_PACKAGE}`:

1. `RoomMemberRequestDto`:
    - Fields matching writable entity fields: `roomId` (UUID), `userId` (UUID), `role` (String).
    - Use Bean Validation annotations: `@NotNull` for `roomId` and `userId`, `@NotBlank` and `@Size(max = 20)` for `role`.
2. `RoomMemberResponseDto`:
    - All entity fields including inherited ones: `id`, `version`, `creationTimestamp`, `updateTimestamp`, `roomId`, `userId`, `role`.

*Note: Use 2-space indentation.*

---

## STEP 3 — Create Mapper

Create `RoomMemberMapper` in `${MAPPER_PACKAGE}` using MapStruct:

```java
package pl.ldz.chat.mapper;

import org.mapstruct.*;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.dto.RoomMemberResponseDto;
import pl.ldz.chat.entity.RoomMember;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMemberMapper {

  @Mapping(target = "roomId", source = "room.id")
  @Mapping(target = "userId", source = "user.id")
  RoomMemberResponseDto toResponseDto(RoomMember entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "room", ignore = true) // Handled in service
  @Mapping(target = "user", ignore = true) // Handled in service
  RoomMember toEntity(RoomMemberRequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "room", ignore = true) // Handled in service
  @Mapping(target = "user", ignore = true) // Handled in service
  void updateEntityFromDto(RoomMemberRequestDto requestDto, @MappingTarget RoomMember entity);

  List<RoomMemberResponseDto> toResponseDtoList(List<RoomMember> entities);
}
```

---

## STEP 4 — Create Repository

Create `RoomMemberRepository` in `${REPO_PACKAGE}` extending `AbstractRepository<RoomMember, UUID>`:

```java
package pl.ldz.chat.repository;

import pl.ldz.chat.entity.RoomMember;
import pl.ldz.chat.repository.base.AbstractRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface RoomMemberRepository extends AbstractRepository<RoomMember, UUID> {
  
  @Query("SELECT rm FROM RoomMember rm WHERE rm.room.id = :roomId")
  List<RoomMember> findByRoomId(@Param("roomId") UUID roomId);
}
```

---

## STEP 5 — Create Service

1. Ensure `CrudService<T, ID, REQ, RES>` exists in `${SERVICE_BASE_PACKAGE}`.
2. Create `RoomMemberService` in `${SERVICE_PACKAGE}`:
    - Implement `CrudService<RoomMember, UUID, RoomMemberRequestDto, RoomMemberResponseDto>`.
    - Use `@Service` and `@RequiredArgsConstructor`.
    - Use `@Transactional` (with `readOnly = true` for GET methods).
    - Handle entity fetching (ChatRoom and User) during `create` and `update`.
    - Throw `EntityNotFoundException` when appropriate.
    - Add `List<RoomMemberResponseDto> findByRoomId(UUID roomId)`.

---

## STEP 6 — Create Controller

Create `RoomMemberController` in `${CONTROLLER_PACKAGE}`:

- Annotate with `@RestController`, `@RequestMapping("/api/v1/room-members")`.
- Expose standard CRUD endpoints:
    - `POST /` -> `create`
    - `GET /{id}` -> `getById`
    - `GET /` -> `getAll` (Paginated)
    - `PUT /{id}` -> `update`
    - `DELETE /{id}` -> `delete`
- Expose `GET /room/{roomId}` -> `findByRoomId`.
- Use `@Valid` for requests.
- Add OpenAPI documentation (`@Operation`, `@ApiResponse`).
- Use `@PreAuthorize` for RBAC (Roles: `USER`, `ADMIN`, `VIEWER`, `AUDITOR`).

---

## STEP 7 — Integration Testing

1. **Repository IT**: Verify custom queries and auditing (timestamps).
2. **Service IT**: Verify business logic, exception handling, and transaction boundaries.
3. **Controller IT**: Verify REST contracts, status codes, validation, and security (roles).
    - Use `AbstractControllerIntegrationTest` with Testcontainers.

---

## GENERAL RULES
- Follow project guidelines in `README.md` and project structure.
- Use Java 21 features.
- Ensure 2-space indentation.
- Maintain consistent error handling via `GlobalExceptionHandler`.
- Use MapStruct for DTO conversions.
