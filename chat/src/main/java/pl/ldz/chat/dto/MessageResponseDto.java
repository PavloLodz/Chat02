package pl.ldz.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {

  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;
  private Long version;

  private UserResponseDto sender;
  private UUID chatRoomId;
  private UUID personalChatId;
  private String content;
  private boolean edited;
  private boolean deleted;
}
