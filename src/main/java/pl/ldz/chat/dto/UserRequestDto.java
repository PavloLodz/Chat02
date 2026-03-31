package pl.ldz.chat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

  @NotBlank(message = "Username is required")
  @Size(max = 50, message = "Username cannot exceed 50 characters")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password hash is required")
  private String passwordHash;

  private String displayName;

  private String avatarUrl;

  @NotNull(message = "Online status is required")
  private Boolean online;
}
