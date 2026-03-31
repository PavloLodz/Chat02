package pl.ldz.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private UUID id;

  private Long version;

  private Instant creationTimestamp;

  private Instant updateTimestamp;

  private String username;

  private String email;

  private String displayName;

  private String avatarUrl;

  private boolean online;
}
