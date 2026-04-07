package pl.ldz.chat.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.repository.UserRepository;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttachmentControllerIT extends AbstractControllerIT {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  private Message testMessage;

  @BeforeEach
  void setUp() {
    User sender = User.builder()
        .username("ctrl_user_" + UUID.randomUUID())
        .email("ctrl_" + UUID.randomUUID() + "@example.com")
        .passwordHash("hash")
        .build();
    userRepository.save(sender);

    testMessage = Message.builder()
        .sender(sender)
        .content("Controller test message")
        .build();
    messageRepository.save(testMessage);
  }

  private AttachmentRequestDto buildRequest(String fileName) {
    return AttachmentRequestDto.builder()
        .messageId(testMessage.getId())
        .fileName(fileName)
        .fileType("application/octet-stream")
        .fileSize(1024L)
        .url("http://storage.example.com/" + fileName)
        .build();
  }

  @Test
  void shouldReturn401WhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/attachments"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn200ForGetAllAsViewer() throws Exception {
    mockMvc.perform(get("/api/v1/attachments"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldCreateAttachmentAsUser() throws Exception {
    String body = objectMapper.writeValueAsString(buildRequest("test.pdf"));

    mockMvc.perform(post("/api/v1/attachments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.fileName").value("test.pdf"))
        .andExpect(jsonPath("$.messageId").value(testMessage.getId().toString()));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn403WhenViewerCreatesAttachment() throws Exception {
    String body = objectMapper.writeValueAsString(buildRequest("forbidden.pdf"));

    mockMvc.perform(post("/api/v1/attachments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturn400ForInvalidRequest() throws Exception {
    AttachmentRequestDto invalid = AttachmentRequestDto.builder()
        .messageId(null) // Invalid
        .fileName("") // Invalid
        .fileType("type")
        .fileSize(-1L) // Invalid
        .url("") // Invalid
        .build();
    String body = objectMapper.writeValueAsString(invalid);

    mockMvc.perform(post("/api/v1/attachments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldReturn404WhenAttachmentNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/attachments/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldUpdateAttachmentAsAdmin() throws Exception {
    // Create first
    String createBody = objectMapper.writeValueAsString(buildRequest("original.txt"));
    String response = mockMvc.perform(post("/api/v1/attachments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
        .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(response).get("id").asText();

    // Update
    AttachmentRequestDto updateRequest = buildRequest("updated.txt");
    updateRequest.setFileSize(2048L);
    String updateBody = objectMapper.writeValueAsString(updateRequest);

    mockMvc.perform(put("/api/v1/attachments/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fileName").value("updated.txt"))
        .andExpect(jsonPath("$.fileSize").value(2048));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldDeleteAttachmentAsUser() throws Exception {
    // Create first
    String createBody = objectMapper.writeValueAsString(buildRequest("to_delete.txt"));
    String response = mockMvc.perform(post("/api/v1/attachments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
        .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(response).get("id").asText();

    // Delete
    mockMvc.perform(delete("/api/v1/attachments/" + id))
        .andExpect(status().isNoContent());

    // Verify deleted
    mockMvc.perform(get("/api/v1/attachments/" + id))
        .andExpect(status().isNotFound());
  }
}
