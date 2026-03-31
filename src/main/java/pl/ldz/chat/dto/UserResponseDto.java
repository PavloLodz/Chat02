package pl.ldz.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
  UUID id,
  Long version,
  Instant creationTimestamp,
  Instant updateTimestamp,
  String username,
  String email,
  String displayName,
  String avatarUrl,
  boolean online
) {}
