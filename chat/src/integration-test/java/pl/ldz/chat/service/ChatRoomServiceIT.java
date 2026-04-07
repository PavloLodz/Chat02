package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.ldz.chat.dto.ChatRoomRequestDto;
import pl.ldz.chat.dto.ChatRoomResponseDto;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.service.base.AbstractServiceIT;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatRoomServiceIT extends AbstractServiceIT {

  @Autowired
  private ChatRoomService chatRoomService;

  @Autowired
  private UserService userService;

  private UserResponseDto testUser;

  @BeforeEach
  void setUp() {
    testUser = userService.create(new UserRequestDto(
        "room_owner_" + UUID.randomUUID(),
        "owner_" + UUID.randomUUID() + "@example.com",
        "pass",
        "Owner",
        null,
        false,
        "USER"
    ));
  }

  private ChatRoomRequestDto buildRequest(String suffix) {
    return new ChatRoomRequestDto(
        "Room " + suffix,
        "Description " + suffix,
        true,
        testUser.id()
    );
  }

  @Test
  void shouldCreateChatRoom() {
    ChatRoomResponseDto response = chatRoomService.create(buildRequest("1"));

    assertThat(response.id()).isNotNull();
    assertThat(response.name()).isEqualTo("Room 1");
    assertThat(response.owner().id()).isEqualTo(testUser.id());
    assertThat(response.creationTimestamp()).isNotNull();
    assertThat(response.updateTimestamp()).isNotNull();
    assertThat(response.version()).isNotNull();
  }

  @Test
  void shouldGetById() {
    ChatRoomResponseDto created = chatRoomService.create(buildRequest("2"));
    ChatRoomResponseDto found = chatRoomService.getById(created.id());

    assertThat(found.name()).isEqualTo("Room 2");
    assertThat(found.id()).isEqualTo(created.id());
  }

  @Test
  void shouldThrowWhenNotFound() {
    UUID randomId = UUID.randomUUID();
    assertThatThrownBy(() -> chatRoomService.getById(randomId))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldReturnPagedChatRooms() {
    chatRoomService.create(buildRequest("3"));
    chatRoomService.create(buildRequest("4"));

    Page<ChatRoomResponseDto> page = chatRoomService.getAll(PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void shouldUpdateChatRoom() {
    ChatRoomResponseDto created = chatRoomService.create(buildRequest("5"));
    ChatRoomRequestDto updateRequest = new ChatRoomRequestDto(
        "Updated Room", "Updated Desc", false, testUser.id()
    );

    ChatRoomResponseDto updated = chatRoomService.update(created.id(), updateRequest);

    assertThat(updated.name()).isEqualTo("Updated Room");
    assertThat(updated.isPublic()).isFalse();
    assertThat(updated.updateTimestamp().toEpochMilli())
            .isGreaterThanOrEqualTo(created.updateTimestamp().toEpochMilli());
  }

  @Test
  void shouldDeleteChatRoom() {
    ChatRoomResponseDto created = chatRoomService.create(buildRequest("6"));
    chatRoomService.delete(created.id());

    assertThatThrownBy(() -> chatRoomService.getById(created.id()))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldVerifyAuditFields() throws InterruptedException {
    ChatRoomResponseDto created = chatRoomService.create(buildRequest("audit"));
    Instant firstUpdate = created.updateTimestamp();
    
    // Small delay to ensure timestamp difference if resolution is high enough
    Thread.sleep(10); 
    
    ChatRoomRequestDto updateRequest = new ChatRoomRequestDto(
        "Renamed", "Desc", true, testUser.id()
    );
    ChatRoomResponseDto updated = chatRoomService.update(created.id(), updateRequest);
    
    assertThat(updated.creationTimestamp().toEpochMilli())
        .isBetween(created.creationTimestamp().toEpochMilli() - 5, created.creationTimestamp().toEpochMilli() + 5);
    assertThat(updated.updateTimestamp().toEpochMilli())
        .isGreaterThanOrEqualTo(firstUpdate.toEpochMilli());
  }
}
