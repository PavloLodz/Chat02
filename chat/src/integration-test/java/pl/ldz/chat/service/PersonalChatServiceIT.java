package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.ldz.chat.dto.PersonalChatRequestDto;
import pl.ldz.chat.dto.PersonalChatResponseDto;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.*;
import pl.ldz.chat.service.base.AbstractServiceIT;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonalChatServiceIT extends AbstractServiceIT {

  @Autowired
  private PersonalChatService personalChatService;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private PersonalChatRepository personalChatRepository;

  @Autowired
  private UserRepository userRepository;

  private User user1;
  private User user2;
  private User user3;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    messageRepository.deleteAll();
    chatRoomRepository.deleteAll();
    personalChatRepository.deleteAll();
    userRepository.deleteAll();

    user1 = userRepository.save(User.builder()
      .username("user1")
      .email("user1@example.com")
      .passwordHash("hash")
      .role("USER")
      .build());

    user2 = userRepository.save(User.builder()
      .username("user2")
      .email("user2@example.com")
      .passwordHash("hash")
      .role("USER")
      .build());

    user3 = userRepository.save(User.builder()
      .username("user3")
      .email("user3@example.com")
      .passwordHash("hash")
      .role("USER")
      .build());
  }

  @Test
  void shouldCreatePersonalChat() {
    PersonalChatRequestDto request = PersonalChatRequestDto.builder()
      .user1Id(user1.getId())
      .user2Id(user2.getId())
      .build();

    PersonalChatResponseDto response = personalChatService.create(request);

    assertThat(response.getId()).isNotNull();
    assertThat(response.getUser1().id()).isEqualTo(user1.getId());
    assertThat(response.getUser2().id()).isEqualTo(user2.getId());
    assertThat(response.getCreationTimestamp()).isNotNull();
    assertThat(response.getUpdateTimestamp()).isNotNull();
  }

  @Test
  void shouldGetPersonalChatById() {
    PersonalChatRequestDto request = PersonalChatRequestDto.builder()
      .user1Id(user1.getId())
      .user2Id(user2.getId())
      .build();
    PersonalChatResponseDto created = personalChatService.create(request);

    PersonalChatResponseDto found = personalChatService.getById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getUser1().id()).isEqualTo(user1.getId());
  }

  @Test
  void shouldGetAllPersonalChatsPaginated() {
    personalChatService.create(new PersonalChatRequestDto(user1.getId(), user2.getId()));
    personalChatService.create(new PersonalChatRequestDto(user1.getId(), user3.getId()));

    Page<PersonalChatResponseDto> page = personalChatService.getAll(PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  void shouldUpdatePersonalChat() {
    PersonalChatResponseDto created = personalChatService.create(new PersonalChatRequestDto(user1.getId(), user2.getId()));
    PersonalChatRequestDto updateRequest = new PersonalChatRequestDto(user1.getId(), user3.getId());

    PersonalChatResponseDto updated = personalChatService.update(created.getId(), updateRequest);

    assertThat(updated.getUser2().id()).isEqualTo(user3.getId());
    // Hibernate increments version on flush/commit, in IT it might stay same until flush
  }

  @Test
  void shouldDeletePersonalChat() {
    PersonalChatResponseDto created = personalChatService.create(new PersonalChatRequestDto(user1.getId(), user2.getId()));

    personalChatService.delete(created.getId());

    assertThatThrownBy(() -> personalChatService.getById(created.getId()))
      .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldThrowNotFoundOnNonExistentId() {
    UUID randomId = UUID.randomUUID();
    assertThatThrownBy(() -> personalChatService.getById(randomId))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
