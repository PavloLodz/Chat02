package pl.ldz.chat.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.entity.Attachment;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepositoryIT;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AttachmentRepositoryIT extends AbstractRepositoryIT {

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldSaveAndFindAttachment() {
    // Given
    User sender = User.builder()
        .username("testuser")
        .email("test@example.com")
        .passwordHash("hash")
        .build();
    userRepository.save(sender);

    Message message = Message.builder()
        .sender(sender)
        .content("Test message")
        .build();
    messageRepository.save(message);

    Attachment attachment = Attachment.attachmentBuilder()
        .message(message)
        .fileName("test.txt")
        .fileType("text/plain")
        .fileSize(100L)
        .url("http://example.com/test.txt")
        .build();

    // When
    Attachment saved = attachmentRepository.save(attachment);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreationTimestamp()).isNotNull();
    assertThat(saved.getUpdateTimestamp()).isNotNull();

    Optional<Attachment> found = attachmentRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getFileName()).isEqualTo("test.txt");
    assertThat(found.get().getMessage().getId()).isEqualTo(message.getId());
  }

  @Test
  void shouldUpdateTimestampOnUpdate() throws InterruptedException {
    // Given
    User sender = User.builder()
        .username("testuser2")
        .email("test2@example.com")
        .passwordHash("hash")
        .build();
    userRepository.save(sender);

    Message message = Message.builder()
        .sender(sender)
        .content("Test message")
        .build();
    messageRepository.save(message);

    Attachment attachment = Attachment.attachmentBuilder()
        .message(message)
        .fileName("test.txt")
        .fileType("text/plain")
        .fileSize(100L)
        .url("http://example.com/test.txt")
        .build();

    Attachment saved = attachmentRepository.saveAndFlush(attachment);
    var firstUpdateTimestamp = saved.getUpdateTimestamp();

    // When
    Thread.sleep(10); // Ensure timestamp difference
    saved.setFileName("updated.txt");
    Attachment updated = attachmentRepository.saveAndFlush(saved);

    // Then
    assertThat(updated.getUpdateTimestamp()).isAfter(firstUpdateTimestamp);
    assertThat(updated.getCreationTimestamp()).isEqualTo(saved.getCreationTimestamp());
  }
}
