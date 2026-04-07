package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.dto.AttachmentResponseDto;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.AbstractServiceIT;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttachmentServiceIT extends AbstractServiceIT {

  @Autowired
  private AttachmentService attachmentService;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  private Message testMessage;

  @BeforeEach
  void setUp() {
    User sender = User.builder()
        .username("serviceuser" + UUID.randomUUID())
        .email("service@example.com" + UUID.randomUUID())
        .passwordHash("hash")
        .build();
    userRepository.save(sender);

    testMessage = Message.builder()
        .sender(sender)
        .content("Service test message")
        .build();
    messageRepository.save(testMessage);
  }

  @Test
  void shouldCreateAttachment() {
    // Given
    AttachmentRequestDto request = AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName("service_test.txt")
        .fileType("text/plain")
        .fileSize(200L)
        .url("http://example.com/service_test.txt")
        .build();

    // When
    AttachmentResponseDto response = attachmentService.create(request);

    // Then
    assertThat(response.getId()).isNotNull();
    assertThat(response.getFileName()).isEqualTo("service_test.txt");
    assertThat(response.getMessageId()).isEqualTo(testMessage.getId());
  }

  @Test
  void shouldGetById() {
    // Given
    AttachmentRequestDto request = AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName("get_by_id.txt")
        .fileType("text/plain")
        .fileSize(300L)
        .url("http://example.com/get_by_id.txt")
        .build();
    AttachmentResponseDto created = attachmentService.create(request);

    // When
    AttachmentResponseDto found = attachmentService.getById(created.getId());

    // Then
    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getFileName()).isEqualTo(created.getFileName());
    assertThat(found.getMessageId()).isEqualTo(created.getMessageId());
    assertThat(found.getCreatedAt()).isNotNull();
    assertThat(found.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldThrowWhenNotFound() {
    assertThatThrownBy(() -> attachmentService.getById(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldUpdateAttachment() {
    // Given
    AttachmentRequestDto request = AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName("original.txt")
        .fileType("text/plain")
        .fileSize(400L)
        .url("http://example.com/original.txt")
        .build();
    AttachmentResponseDto created = attachmentService.create(request);

    AttachmentRequestDto updateRequest = AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName("updated.txt")
        .fileType("text/plain")
        .fileSize(500L)
        .url("http://example.com/updated.txt")
        .build();

    // When
    AttachmentResponseDto updated = attachmentService.update(created.getId(), updateRequest);

    // Then
    assertThat(updated.getFileName()).isEqualTo("updated.txt");
    assertThat(updated.getFileSize()).isEqualTo(500L);
    assertThat(updated.getId()).isEqualTo(created.getId());
  }

  @Test
  void shouldDeleteAttachment() {
    // Given
    AttachmentRequestDto request = AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName("to_delete.txt")
        .fileType("text/plain")
        .fileSize(600L)
        .url("http://example.com/to_delete.txt")
        .build();
    AttachmentResponseDto created = attachmentService.create(request);

    // When
    attachmentService.delete(created.getId());

    // Then
    assertThatThrownBy(() -> attachmentService.getById(created.getId()))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldGetAllPaginated() {
    // Given
    for (int i = 0; i < 5; i++) {
      attachmentService.create(AttachmentRequestDto.builder()
          .messageId(testMessage.getId())
          .fileName("file" + i + ".txt")
          .fileType("text/plain")
          .fileSize(100L)
          .url("url" + i)
          .build());
    }

    // When
    Page<AttachmentResponseDto> page = attachmentService.getAll(PageRequest.of(0, 2));

    // Then
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(5);
  }
}
