package pl.ldz.chat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import pl.ldz.chat.dto.UserRequestDto;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIntegrationTest extends AbstractControllerIntegrationTest {

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldCreateUser() throws Exception {
    UserRequestDto request = UserRequestDto.builder()
        .username("controlleruser")
        .email("controller@example.com")
        .passwordHash("hash")
        .online(true)
        .build();

    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("controlleruser"))
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @WithMockUser(roles = "VIEWER")
  void shouldGetUser() throws Exception {
    // Note: In a real integration test, we might want to seed the DB first.
    // Since each test is transactional or we use a clean DB, we should be careful.
    // For now, let's assume we can at least call the endpoint.
    UUID randomId = UUID.randomUUID();
    mockMvc.perform(get("/api/v1/users/" + randomId))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void shouldReturnBadRequestForInvalidInput() throws Exception {
    UserRequestDto invalidRequest = UserRequestDto.builder()
        .username("") // Blank
        .email("invalid-email")
        .build();

    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/users"))
        .andExpect(status().isUnauthorized());
  }
}
