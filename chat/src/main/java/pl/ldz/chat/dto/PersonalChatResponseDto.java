package pl.ldz.chat.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalChatResponseDto {
  private UUID id;
  private UserResponseDto user1;
  private UserResponseDto user2;
  private Long version;
  private Instant creationTimestamp;
  private Instant updateTimestamp;
}
