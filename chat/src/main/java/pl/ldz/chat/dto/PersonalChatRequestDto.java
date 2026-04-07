package pl.ldz.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalChatRequestDto {

  @NotNull
  private UUID user1Id;

  @NotNull
  private UUID user2Id;
}
