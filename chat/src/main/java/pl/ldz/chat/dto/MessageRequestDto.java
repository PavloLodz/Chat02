package pl.ldz.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDto {

  @NotNull
  private UUID senderId;

  private UUID chatRoomId;

  private UUID personalChatId;

  @NotBlank
  @Size(max = 2000)
  private String content;
}
