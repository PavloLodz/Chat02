package pl.ldz.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the {@code UserResponseDto} record from the source project.
 *
 * <p>All fields correspond 1-to-1 with the JSON produced by the API:
 * <pre>
 * {
 *   "id":                "550e8400-...",
 *   "version":           0,
 *   "creationTimestamp": "2025-01-15T10:00:00Z",
 *   "updateTimestamp":   "2025-01-15T10:00:00Z",
 *   "username":          "johndoe",
 *   "email":             "john@example.com",
 *   "displayName":       "John Doe",
 *   "avatarUrl":         null,
 *   "online":            false,
 *   "role":              "USER"
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("creationTimestamp")
    private Instant creationTimestamp;


    @JsonProperty("updateTimestamp")
    // Unknown class: 'InstantDeserializer.INSTANT'
    private Instant updateTimestamp;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("avatarUrl")
    private String avatarUrl;

    @JsonProperty("online")
    private boolean online;

    @JsonProperty("role")
    private String role;
}
