package pl.ldz.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatRoomControllerIT extends AbstractControllerIT {

  private String createOwnerAndReturnId() throws Exception {
    UUID rnd = UUID.randomUUID();
    String userBody = objectMapper.writeValueAsString(new pl.ldz.chat.dto.UserRequestDto(
        "owner_" + rnd,
        "owner_" + rnd + "@example.com",
        "pass",
        "Owner",
        null,
        false,
        "USER"
    ));

    String response = mockMvc.perform(post("/api/v1/users").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(userBody))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    JsonNode node = objectMapper.readTree(response);
    return node.get("id").asText();
  }

  private String buildCreateRoomBody(String name, String description, boolean isPublic, String ownerId) throws Exception {
    var dto = new pl.ldz.chat.dto.ChatRoomRequestDto(name, description, isPublic, UUID.fromString(ownerId));
    return objectMapper.writeValueAsString(dto);
  }

  @Test
  void shouldReturn401WhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/chat-rooms"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn200ForGetAllAsViewer() throws Exception {
    mockMvc.perform(get("/api/v1/chat-rooms"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldCreateChatRoomAsUser() throws Exception {
    String ownerId = createOwnerAndReturnId();
    String body = buildCreateRoomBody("General", "Desc", true, ownerId);

    mockMvc.perform(post("/api/v1/chat-rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.name").value("General"))
        .andExpect(jsonPath("$.owner.id").value(ownerId))
        .andExpect(jsonPath("$.creationTimestamp").isNotEmpty())
        .andExpect(jsonPath("$.updateTimestamp").isNotEmpty());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturn422ForInvalidRequest() throws Exception {
    String ownerId = createOwnerAndReturnId();
    // invalid: empty name
    String body = buildCreateRoomBody("", "Desc", true, ownerId);

    mockMvc.perform(post("/api/v1/chat-rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturn404WhenNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/chat-rooms/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "USER")
  void shouldSupportPagination() throws Exception {
    String ownerId = createOwnerAndReturnId();
    // create few rooms
    for (int i = 0; i < 3; i++) {
      String body = buildCreateRoomBody("Room" + i, "D" + i, true, ownerId);
      mockMvc.perform(post("/api/v1/chat-rooms")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isCreated());
    }

    mockMvc.perform(get("/api/v1/chat-rooms?page=0&size=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").exists());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDeleteChatRoom() throws Exception {
    String ownerId = createOwnerAndReturnId();
    String body = buildCreateRoomBody("TBD", "D", true, ownerId);
    String response = mockMvc.perform(post("/api/v1/chat-rooms")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    String id = objectMapper.readTree(response).get("id").asText();

    mockMvc.perform(delete("/api/v1/chat-rooms/" + id))
        .andExpect(status().isNoContent());
  }
}
