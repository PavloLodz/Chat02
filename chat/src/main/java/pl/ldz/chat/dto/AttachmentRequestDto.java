package pl.ldz.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequestDto {

  @NotNull
  private UUID messageId;

  @NotBlank
  private String fileName;

  @NotBlank
  private String fileType;

  @NotNull
  @Min(0)
  private Long fileSize;

  @NotBlank
  private String url;
}
