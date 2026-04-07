package pl.ldz.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDto {

  private UUID id;
  private Long version;
  private Instant creationTimestamp;
  private Instant updateTimestamp;

  private UUID messageId;
  private String fileName;
  private String fileType;
  private Long fileSize;
  private String url;
}
