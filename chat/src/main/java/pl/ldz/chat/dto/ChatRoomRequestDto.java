package pl.ldz.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatRoomRequestDto(
  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name cannot exceed 100 characters")
  String name,

  String description,

  boolean isPublic,

  @NotNull(message = "Owner ID is required")
  UUID ownerId
) {}
