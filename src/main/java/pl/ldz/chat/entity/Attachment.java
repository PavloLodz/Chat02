package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attachments")
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

  public Attachment() {}

  public Message getMessage() { return message; }
  public void setMessage(Message message) { this.message = message; }

  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }

  public String getFileType() { return fileType; }
  public void setFileType(String fileType) { this.fileType = fileType; }

  public Long getFileSize() { return fileSize; }
  public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }
}
