package pl.ldz.chat.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
// @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder @Value
// TODO: make it immutable
public class LoginRequestDto {

  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
