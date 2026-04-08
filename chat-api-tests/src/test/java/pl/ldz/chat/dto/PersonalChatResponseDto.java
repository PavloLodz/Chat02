package pl.ldz.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the {@code PersonalChatResponseDto} class from the source project.
 *
 * <pre>
 * {
 *   "id":                "550e8400-...",
 *   "version":           0,
 *   "creationTimestamp": "2025-01-15T10:00:00Z",
 *   "updateTimestamp":   "2025-01-15T10:00:00Z",
 *   "user1":             { ... },
 *   "user2":             { ... }
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalChatResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("creationTimestamp")
    private Instant creationTimestamp;

    @JsonProperty("updateTimestamp")
    private Instant updateTimestamp;

    @JsonProperty("user1")
    private UserResponseDto user1;

    @JsonProperty("user2")
    private UserResponseDto user2;
}
