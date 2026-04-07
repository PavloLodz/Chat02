package pl.ldz.chat.api.common;

import java.util.UUID;

/**
 * Builds JSON payloads for the {@code AttachmentRequestDto} shape.
 *
 * <p>Validation constraints from the source project:
 * <ul>
 *   <li>{@code messageId} – not null, must reference an existing message</li>
 *   <li>{@code fileName}  – not blank</li>
 *   <li>{@code fileType}  – not blank</li>
 *   <li>{@code fileSize}  – not null, min 0</li>
 *   <li>{@code url}       – not blank</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * String body = AttachmentPayload.valid(messageId).build();
 * String body = AttachmentPayload.builder()
 *     .messageId(someId)
 *     .fileName("report.pdf")
 *     .fileType("application/pdf")
 *     .fileSize(2048L)
 *     .url("http://storage.example.com/report.pdf")
 *     .build();
 * </pre>
 */
public final class AttachmentPayload {

    private AttachmentPayload() {}

    /**
     * Returns a valid attachment payload referencing the given {@code messageId}.
     * A unique filename is generated to avoid conflicts across test runs.
     */
    public static Builder valid(UUID messageId) {
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return builder()
                .messageId(messageId)
                .fileName("file_" + uid + ".pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .url("http://storage.example.com/file_" + uid + ".pdf");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID   messageId = null;
        private String fileName  = "file.pdf";
        private String fileType  = "application/pdf";
        private Long   fileSize  = 1024L;
        private String url       = "http://storage.example.com/file.pdf";

        public Builder messageId(UUID v)  { this.messageId = v; return this; }
        public Builder fileName(String v) { this.fileName  = v; return this; }
        public Builder fileType(String v) { this.fileType  = v; return this; }
        public Builder fileSize(Long v)   { this.fileSize  = v; return this; }
        public Builder url(String v)      { this.url       = v; return this; }

        /** Returns a JSON string matching {@code AttachmentRequestDto}. */
        public String build() {
            String msgId = messageId == null ? "null" : "\"" + messageId + "\"";
            String fs    = fileSize  == null ? "null" : fileSize.toString();
            String fn    = fileName  == null ? "null" : "\"" + fileName + "\"";
            String ft    = fileType  == null ? "null" : "\"" + fileType + "\"";
            String u     = url       == null ? "null" : "\"" + url + "\"";
            return """
                    {
                      "messageId": %s,
                      "fileName":  %s,
                      "fileType":  %s,
                      "fileSize":  %s,
                      "url":       %s
                    }
                    """.formatted(msgId, fn, ft, fs, u);
        }
    }
}
