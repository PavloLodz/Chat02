package pl.ldz.chat.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mirrors {@code pl.ldz.chat.dto.auth.LoginRequestDto} from the source project.
 * Used to serialise login requests via Jackson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
