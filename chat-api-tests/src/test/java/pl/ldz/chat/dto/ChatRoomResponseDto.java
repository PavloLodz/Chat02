package pl.ldz.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the {@code ChatRoomResponseDto} record from the source project.
 *
 * <pre>
 * {
 *   "id":                "550e8400-...",
 *   "version":           0,
 *   "creationTimestamp": "2025-01-15T10:00:00Z",
 *   "updateTimestamp":   "2025-01-15T10:00:00Z",
 *   "name":              "General",
 *   "description":       "General discussion",
 *   "isPublic":          true,
 *   "owner":             { ... }
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("creationTimestamp")
    private Instant creationTimestamp;

    @JsonProperty("updateTimestamp")
    private Instant updateTimestamp;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isPublic")
    private boolean isPublic;

    @JsonProperty("owner")
    private UserResponseDto owner;
}
