package pl.ldz.chat.dto.auth;

import java.time.LocalDateTime;

import lombok.*;

@Getter
//@Setter
// @NoArgsConstructor
@AllArgsConstructor
@Builder @Value
public class JwtResponseDto {

  private String token;
  private LocalDateTime expiresAt;
}
