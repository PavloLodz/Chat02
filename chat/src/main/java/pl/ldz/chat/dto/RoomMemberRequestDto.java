package pl.ldz.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RoomMemberRequestDto(
  @NotNull(message = "Room ID is required")
  UUID roomId,

  @NotNull(message = "User ID is required")
  UUID userId,

  @NotBlank(message = "Role is required")
  @Size(max = 20, message = "Role must not exceed 20 characters")
  String role
) {}
