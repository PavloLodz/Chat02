package pl.ldz.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mirrors the {@code UserRequestDto} record from the source project.
 *
 * <p>Field names and semantics are identical to the source:
 * <ul>
 *   <li>{@code username}     – required, max 50 chars</li>
 *   <li>{@code email}        – required, valid e-mail format</li>
 *   <li>{@code passwordHash} – required (raw password; server encodes it)</li>
 *   <li>{@code displayName}  – optional</li>
 *   <li>{@code avatarUrl}    – optional</li>
 *   <li>{@code online}       – defaults to {@code false}</li>
 *   <li>{@code role}         – optional, e.g. "USER", "ADMIN"</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequestDto {

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    /** Sent as {@code passwordHash} – the server BCrypt-encodes the value. */
    @JsonProperty("passwordHash")
    private String passwordHash;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("avatarUrl")
    private String avatarUrl;

    @JsonProperty("online")
    private boolean online;

    @JsonProperty("role")
    private String role;
}
