package pl.ldz.chat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.RoomMember;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepositoryIT;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoomMemberRepositoryIT extends AbstractRepositoryIT {

  @Autowired
  private RoomMemberRepository roomMemberRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  private User testUser;
  private ChatRoom testRoom;

  @BeforeEach
  void setUp() {
    roomMemberRepository.deleteAll();
    chatRoomRepository.deleteAll();
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
  void shouldSaveAndFindRoomMember() {
    RoomMember member = RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("ADMIN")
        .build();

    RoomMember saved = roomMemberRepository.save(member);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreationTimestamp()).isNotNull();
    assertThat(saved.getUpdateTimestamp()).isNotNull();
    assertThat(saved.getRole()).isEqualTo("ADMIN");

    RoomMember found = roomMemberRepository.findById(saved.getId()).orElseThrow();
    assertThat(found.getUser().getUsername()).isEqualTo("testuser");
    assertThat(found.getRoom().getName()).isEqualTo("Test Room");
  }

  @Test
  void shouldFindByRoomId() {
    RoomMember member = RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build();
    roomMemberRepository.save(member);

    List<RoomMember> members = roomMemberRepository.findByRoomId(testRoom.getId());

    assertThat(members).hasSize(1);
    assertThat(members.get(0).getRoom().getId()).isEqualTo(testRoom.getId());
  }

  @Test
  void shouldUpdateTimestampOnUpdate() throws InterruptedException {
    RoomMember member = RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build();
    RoomMember saved = roomMemberRepository.saveAndFlush(member);
    var initialUpdateTimestamp = saved.getUpdateTimestamp();

    // Small delay to ensure timestamp difference
    Thread.sleep(10);

    saved.setRole("MANAGER");
    RoomMember updated = roomMemberRepository.saveAndFlush(saved);

    assertThat(updated.getUpdateTimestamp()).isAfter(initialUpdateTimestamp);
    assertThat(updated.getCreationTimestamp()).isEqualTo(saved.getCreationTimestamp());
  }
}
