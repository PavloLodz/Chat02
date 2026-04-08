package pl.ldz.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.PersonalChatRequestDto;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonalChatControllerIT extends AbstractControllerIT {

  @Autowired
  private AttachmentRepository attachmentRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  @Autowired
  private PersonalChatRepository personalChatRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoomMemberRepository roomMemberRepository;

  private User user1;
  private User user2;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    friendRequestRepository.deleteAll();
    messageRepository.deleteAll();
    roomMemberRepository.deleteAll();
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
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldCreatePersonalChat() throws Exception {
    PersonalChatRequestDto request = new PersonalChatRequestDto(user1.getId(), user2.getId());

    mockMvc.perform(post("/api/v1/personal-chats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.user1.id").value(user1.getId().toString()))
      .andExpect(jsonPath("$.user2.id").value(user2.getId().toString()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldGetPersonalChatById() throws Exception {
    PersonalChatRequestDto request = new PersonalChatRequestDto(user1.getId(), user2.getId());
    String response = mockMvc.perform(post("/api/v1/personal-chats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    mockMvc.perform(get("/api/v1/personal-chats/{id}", id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturnForbiddenWhenUserTriesToGetAll() throws Exception {
    mockMvc.perform(get("/api/v1/personal-chats"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldUpdatePersonalChat() throws Exception {
    PersonalChatRequestDto request = new PersonalChatRequestDto(user1.getId(), user2.getId());
    String response = mockMvc.perform(post("/api/v1/personal-chats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    PersonalChatRequestDto updateRequest = new PersonalChatRequestDto(user1.getId(), user2.getId());

    mockMvc.perform(put("/api/v1/personal-chats/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDeletePersonalChat() throws Exception {
    PersonalChatRequestDto request = new PersonalChatRequestDto(user1.getId(), user2.getId());
    String response = mockMvc.perform(post("/api/v1/personal-chats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    mockMvc.perform(delete("/api/v1/personal-chats/{id}", id))
      .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/personal-chats/{id}", id))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturn422OnValidationFailure() throws Exception {
    PersonalChatRequestDto invalidRequest = new PersonalChatRequestDto(null, null);

    mockMvc.perform(post("/api/v1/personal-chats")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isUnprocessableEntity());
  }
}
