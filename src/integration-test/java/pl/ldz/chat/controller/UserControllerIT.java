package pl.ldz.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.UserRequestDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractControllerIT {

  private UserRequestDto buildRequest(String suffix) {
    return new UserRequestDto(
      "ctrl_" + suffix,
      "ctrl_" + suffix + "@example.com",
      "hash_" + suffix,
      "Display " + suffix,
      null,
      false,
      ""
    );
  }

  @Test
  void shouldReturn401WhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/users"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn200ForGetAllAsViewer() throws Exception {
    mockMvc.perform(get("/api/v1/users"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldCreateUserAsAdmin() throws Exception {
    String body = objectMapper.writeValueAsString(buildRequest("c1"));

    mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").isNotEmpty())
      .andExpect(jsonPath("$.username").value("ctrl_c1"));
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldReturn403WhenViewerCreatesUser() throws Exception {
    String body = objectMapper.writeValueAsString(buildRequest("c2"));

    mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturn422ForInvalidRequest() throws Exception {
    UserRequestDto invalid = new UserRequestDto("", "not-an-email", "", null, null, false, "");
    String body = objectMapper.writeValueAsString(invalid);

    mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isUnprocessableEntity());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturn404WhenUserNotFound() throws Exception {
    mockMvc.perform(get("/api/v1/users/00000000-0000-0000-0000-000000000000"))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldDeleteUser() throws Exception {
    String body = objectMapper.writeValueAsString(buildRequest("del1"));
    String response = mockMvc.perform(post("/api/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();

    String id = objectMapper.readTree(response).get("id").asText();

    mockMvc.perform(delete("/api/v1/users/" + id))
      .andExpect(status().isNoContent());
  }
}
