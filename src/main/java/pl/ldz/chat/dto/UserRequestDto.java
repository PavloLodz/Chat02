package pl.ldz.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(

  @NotBlank(message = "Username is required")
  @Size(max = 50, message = "Username must not exceed 50 characters")
  String username,

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  String email,

  @NotBlank(message = "Password is required")
  String passwordHash,

  String displayName,

  String avatarUrl,

  boolean online,

  String role
) {}
