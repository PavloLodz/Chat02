package pl.ldz.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record ChatRoomResponseDto(
  UUID id,
  Long version,
  Instant creationTimestamp,
  Instant updateTimestamp,
  String name,
  String description,
  boolean isPublic,
  UserResponseDto owner
) {}
