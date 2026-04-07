package pl.ldz.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.MessageRequestDto;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessageControllerIT extends AbstractControllerIT {

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
  @WithMockUser(roles = "USER")
  void shouldCreateMessage() throws Exception {
    MessageRequestDto request = MessageRequestDto.builder()
      .senderId(sender.getId())
      .personalChatId(personalChat.getId())
      .content("Hello World")
      .build();

    mockMvc.perform(post("/api/v1/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.content").value("Hello World"))
      .andExpect(jsonPath("$.sender.username").value("sender"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldGetMessageById() throws Exception {
    MessageRequestDto request = new MessageRequestDto(sender.getId(), null, personalChat.getId(), "Test Msg");
    String response = mockMvc.perform(post("/api/v1/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    mockMvc.perform(get("/api/v1/messages/{id}", id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.content").value("Test Msg"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldUpdateMessage() throws Exception {
    MessageRequestDto request = new MessageRequestDto(sender.getId(), null, personalChat.getId(), "Original");
    String response = mockMvc.perform(post("/api/v1/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    MessageRequestDto updateRequest = new MessageRequestDto(sender.getId(), null, personalChat.getId(), "Updated Content");

    mockMvc.perform(put("/api/v1/messages/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.content").value("Updated Content"))
      .andExpect(jsonPath("$.edited").value(true));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldDeleteMessage() throws Exception {
    MessageRequestDto request = new MessageRequestDto(sender.getId(), null, personalChat.getId(), "To Delete");
    String response = mockMvc.perform(post("/api/v1/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

    mockMvc.perform(delete("/api/v1/messages/{id}", id))
      .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/messages/{id}", id))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturn422OnValidationFailure() throws Exception {
    MessageRequestDto invalidRequest = new MessageRequestDto(null, null, null, "");

    mockMvc.perform(post("/api/v1/messages")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isUnprocessableEntity());
  }
}
