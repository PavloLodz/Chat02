package pl.ldz.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors the {@code AttachmentResponseDto} record from the source project.
 *
 * <p>All fields correspond 1-to-1 with the JSON produced by
 * {@code GET /api/v1/attachments/{id}} and {@code POST /api/v1/attachments}.
 *
 * <pre>
 * {
 *   "id":                "550e8400-...",
 *   "version":           0,
 *   "creationTimestamp": "2025-01-15T10:00:00Z",
 *   "updateTimestamp":   "2025-01-15T10:00:00Z",
 *   "messageId":         "660e9400-...",
 *   "fileName":          "document.pdf",
 *   "fileType":          "application/pdf",
 *   "fileSize":          1024,
 *   "url":               "http://storage.example.com/document.pdf"
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDto {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("creationTimestamp")
    private Instant creationTimestamp;

    @JsonProperty("updateTimestamp")
    private Instant updateTimestamp;

    @JsonProperty("messageId")
    private UUID messageId;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileType")
    private String fileType;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("url")
    private String url;
}
