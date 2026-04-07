package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.ldz.chat.dto.MessageRequestDto;
import pl.ldz.chat.dto.MessageResponseDto;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.AttachmentRepository;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.repository.PersonalChatRepository;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.AbstractServiceIT;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageServiceIT extends AbstractServiceIT {

  @Autowired
  private MessageService messageService;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private PersonalChatRepository personalChatRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private UserRepository userRepository;

  private User sender;
  private PersonalChat personalChat;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    messageRepository.deleteAll();
    chatRoomRepository.deleteAll();
    personalChatRepository.deleteAll();
    userRepository.deleteAll();

    sender = userRepository.save(User.builder()
      .username("sender")
      .email("sender@example.com")
      .passwordHash("hash")
      .role("USER")
      .build());

    User otherUser = userRepository.save(User.builder()
      .username("other")
      .email("other@example.com")
      .passwordHash("hash")
      .role("USER")
      .build());

    personalChat = personalChatRepository.save(PersonalChat.builder()
      .user1(sender)
      .user2(otherUser)
      .build());
  }

  @Test
  void shouldCreateMessage() {
    MessageRequestDto request = MessageRequestDto.builder()
      .senderId(sender.getId())
      .personalChatId(personalChat.getId())
      .content("Hello")
      .build();

    MessageResponseDto response = messageService.create(request);

    assertThat(response.getId()).isNotNull();
    assertThat(response.getContent()).isEqualTo("Hello");
    assertThat(response.getSender().id()).isEqualTo(sender.getId());
    assertThat(response.getPersonalChatId()).isEqualTo(personalChat.getId());
    assertThat(response.isEdited()).isFalse();
    assertThat(response.getCreatedAt()).isNotNull();
  }

  @Test
  void shouldGetMessageById() {
    MessageRequestDto request = MessageRequestDto.builder()
      .senderId(sender.getId())
      .personalChatId(personalChat.getId())
      .content("Hello")
      .build();
    MessageResponseDto created = messageService.create(request);

    MessageResponseDto found = messageService.getById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getContent()).isEqualTo("Hello");
  }

  @Test
  void shouldGetAllMessagesPaginated() {
    messageService.create(new MessageRequestDto(sender.getId(), null, personalChat.getId(), "M1"));
    messageService.create(new MessageRequestDto(sender.getId(), null, personalChat.getId(), "M2"));

    Page<MessageResponseDto> page = messageService.getAll(PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  void shouldUpdateMessage() {
    MessageResponseDto created = messageService.create(new MessageRequestDto(sender.getId(), null, personalChat.getId(), "Original"));
    MessageRequestDto updateRequest = new MessageRequestDto(sender.getId(), null, personalChat.getId(), "Updated");

    MessageResponseDto updated = messageService.update(created.getId(), updateRequest);

    assertThat(updated.getContent()).isEqualTo("Updated");
    assertThat(updated.isEdited()).isTrue();
  }

  @Test
  void shouldDeleteMessage() {
    MessageResponseDto created = messageService.create(new MessageRequestDto(sender.getId(), null, personalChat.getId(), "To Delete"));

    messageService.delete(created.getId());

    assertThatThrownBy(() -> messageService.getById(created.getId()))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrowOnMissingChatReference() {
    MessageRequestDto request = MessageRequestDto.builder()
      .senderId(sender.getId())
      .content("Orphan")
      .build();

    assertThatThrownBy(() -> messageService.create(request))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("must belong to either a ChatRoom or a PersonalChat");
  }
}
