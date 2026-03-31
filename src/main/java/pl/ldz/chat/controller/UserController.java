package pl.ldz.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users")
public class UserController {

  private final UserService userService;

  @PostMapping
  @Operation(summary = "Create a new user")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID")
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @GetMapping
  @Operation(summary = "Get all users (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of users")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  public ResponseEntity<Page<UserResponseDto>> getAll(@ParameterObject Pageable pageable) {
    return ResponseEntity.ok(userService.getAll(pageable));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user")
  @ApiResponse(responseCode = "200", description = "User updated successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<UserResponseDto> update(
      @PathVariable UUID id, @Valid @RequestBody UserRequestDto request) {
    return ResponseEntity.ok(userService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user")
  @ApiResponse(responseCode = "204", description = "User deleted successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
