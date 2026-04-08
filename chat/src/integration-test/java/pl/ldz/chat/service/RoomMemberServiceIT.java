package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.dto.RoomMemberResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.AttachmentRepository;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.FriendRequestRepository;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.repository.PersonalChatRepository;
import pl.ldz.chat.repository.RoomMemberRepository;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.AbstractServiceIT;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class RoomMemberServiceIT extends AbstractServiceIT {

  @Autowired
  private RoomMemberService roomMemberService;

  @Autowired
  private RoomMemberRepository roomMemberRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private PersonalChatRepository personalChatRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  private User testUser;
  private ChatRoom testRoom;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    friendRequestRepository.deleteAll();
    messageRepository.deleteAll();
    roomMemberRepository.deleteAll();
    chatRoomRepository.deleteAll();
    personalChatRepository.deleteAll();
    userRepository.deleteAll();

    testUser = userRepository.save(User.builder()
        .username("testuser")
        .email("test@example.com")
        .passwordHash("hash")
        .role("USER")
        .build());

    testRoom = chatRoomRepository.save(ChatRoom.builder()
        .name("Test Room")
        .owner(testUser)
        .build());
  }

  @Test
  void shouldCreateRoomMember() {
    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "ADMIN");

    RoomMemberResponseDto response = roomMemberService.create(request);

    assertThat(response.id()).isNotNull();
    assertThat(response.role()).isEqualTo("ADMIN");
    assertThat(response.roomId()).isEqualTo(testRoom.getId());
    assertThat(response.userId()).isEqualTo(testUser.getId());
  }

  @Test
  void shouldGetById() {
    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MEMBER");
    RoomMemberResponseDto saved = roomMemberService.create(request);

    RoomMemberResponseDto found = roomMemberService.getById(saved.id());

    assertThat(found).isEqualTo(saved);
  }

  @Test
  void shouldThrowWhenNotFound() {
    UUID randomId = UUID.randomUUID();
    assertThatThrownBy(() -> roomMemberService.getById(randomId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("RoomMember with id '" + randomId + "' not found");
  }

  @Test
  void shouldUpdateRoomMember() {
    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MEMBER");
    RoomMemberResponseDto saved = roomMemberService.create(request);

    RoomMemberRequestDto updateRequest = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MANAGER");
    RoomMemberResponseDto updated = roomMemberService.update(saved.id(), updateRequest);

    assertThat(updated.role()).isEqualTo("MANAGER");
    assertThat(updated.id()).isEqualTo(saved.id());
  }

  @Test
  void shouldDeleteRoomMember() {
    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MEMBER");
    RoomMemberResponseDto saved = roomMemberService.create(request);

    roomMemberService.delete(saved.id());

    assertThat(roomMemberRepository.existsById(saved.id())).isFalse();
  }

  @Test
  void shouldGetAllPaginated() {
    roomMemberService.create(new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MEMBER"));

    Page<RoomMemberResponseDto> all = roomMemberService.getAll(PageRequest.of(0, 10));

    assertThat(all.getContent()).hasSize(1);
    assertThat(all.getTotalElements()).isEqualTo(1);
  }

  @Test
  void shouldFindByRoomId() {
    roomMemberService.create(new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MEMBER"));

    List<RoomMemberResponseDto> members = roomMemberService.findByRoomId(testRoom.getId());

    assertThat(members).hasSize(1);
    assertThat(members.get(0).roomId()).isEqualTo(testRoom.getId());
  }
}
