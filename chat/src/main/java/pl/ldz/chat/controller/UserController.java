package pl.ldz.chat.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
@RequiredArgsConstructor
public class UserController {

  private final CrudService<pl.ldz.chat.entity.User, UUID, UserRequestDto, UserResponseDto> userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a new user")
  @ApiResponse(responseCode = "201", description = "User created")
  @ApiResponse(responseCode = "422", description = "Validation error")
  public UserResponseDto create(@Valid @RequestBody UserRequestDto request) {
    return userService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get user by ID")
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "404", description = "User not found")
  public UserResponseDto getById(@PathVariable UUID id) {
    return userService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get all users (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of users")
  public Page<UserResponseDto> getAll(Pageable pageable) {
    return userService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update a user")
  @ApiResponse(responseCode = "200", description = "User updated")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "422", description = "Validation error")
  public UserResponseDto update(@PathVariable UUID id, @Valid @RequestBody UserRequestDto request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a user")
  @ApiResponse(responseCode = "204", description = "User deleted")
  @ApiResponse(responseCode = "404", description = "User not found")
  public void delete(@PathVariable UUID id) {
    userService.delete(id);
  }
}
