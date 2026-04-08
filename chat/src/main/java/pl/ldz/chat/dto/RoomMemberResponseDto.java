package pl.ldz.chat.dto;

import java.time.Instant;
import java.util.UUID;

public record RoomMemberResponseDto(
  UUID id,
  Long version,
  Instant creationTimestamp,
  Instant updateTimestamp,
  UUID roomId,
  UUID userId,
  String role
) {}
