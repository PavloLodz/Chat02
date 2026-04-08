package pl.ldz.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.RoomMember;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RoomMemberControllerIT extends AbstractControllerIT {

  @Autowired
  private RoomMemberRepository roomMemberRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private PersonalChatRepository personalChatRepository;

  @Autowired
  private AttachmentRepository attachmentRepository;

  private User testUser;
  private ChatRoom testRoom;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    friendRequestRepository.deleteAll();
    messageRepository.deleteAll();
    roomMemberRepository.deleteAll();
    personalChatRepository.deleteAll();
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
  @WithMockUser(roles = "USER")
  void shouldCreateRoomMember() throws Exception {
    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "ADMIN");

    mockMvc.perform(post("/api/v1/room-members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.role").value("ADMIN"))
        .andExpect(jsonPath("$.roomId").value(testRoom.getId().toString()))
        .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldGetRoomMemberById() throws Exception {
    RoomMember member = roomMemberRepository.save(RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build());

    mockMvc.perform(get("/api/v1/room-members/{id}", member.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(member.getId().toString()))
        .andExpect(jsonPath("$.role").value("MEMBER"));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldGetAllRoomMembers() throws Exception {
    roomMemberRepository.save(RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build());

    mockMvc.perform(get("/api/v1/room-members"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldUpdateRoomMember() throws Exception {
    RoomMember member = roomMemberRepository.save(RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build());

    RoomMemberRequestDto request = new RoomMemberRequestDto(testRoom.getId(), testUser.getId(), "MANAGER");

    mockMvc.perform(put("/api/v1/room-members/{id}", member.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("MANAGER"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDeleteRoomMember() throws Exception {
    RoomMember member = roomMemberRepository.save(RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build());

    mockMvc.perform(delete("/api/v1/room-members/{id}", member.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/room-members/{id}", member.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldGetMembersByRoomId() throws Exception {
    roomMemberRepository.save(RoomMember.builder()
        .room(testRoom)
        .user(testUser)
        .role("MEMBER")
        .build());

    mockMvc.perform(get("/api/v1/room-members/room/{roomId}", testRoom.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].roomId").value(testRoom.getId().toString()));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn404WhenNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/room-members/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturn400WhenValidationFails() throws Exception {
    RoomMemberRequestDto invalidRequest = new RoomMemberRequestDto(null, null, "");

    mockMvc.perform(post("/api/v1/room-members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isUnprocessableEntity()) // Based on GlobalExceptionHandler using 422
        .andExpect(jsonPath("$.title").value("Validation Error"));
  }
}
