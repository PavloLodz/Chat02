package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "url", nullable = false)
  private String url;

  @Builder(builderMethodName = "attachmentBuilder")
  public Attachment(UUID id, Long version, java.time.Instant creationTimestamp, java.time.Instant updateTimestamp, Message message, String fileName, String fileType, Long fileSize, String url) {
    this.setId(id);
    this.setVersion(version);
    this.setCreationTimestamp(creationTimestamp);
    this.setUpdateTimestamp(updateTimestamp);
    this.message = message;
    this.fileName = fileName;
    this.fileType = fileType;
    this.fileSize = fileSize;
    this.url = url;
  }

  @Override
  public String toString() {
    return "Attachment{" +
      "id=" + getId() +
      ", fileName='" + fileName + '\'' +
      ", url='" + url + '\'' +
      '}';
  }
}
