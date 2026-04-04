package pl.ldz.chat.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mirrors {@code pl.ldz.chat.dto.auth.JwtResponseDto} from the source project.
 * Used to deserialise the login response via Jackson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDto {

    @JsonProperty("token")
    private String token;

    @JsonProperty("expiresAt")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime expiresAt;
}
