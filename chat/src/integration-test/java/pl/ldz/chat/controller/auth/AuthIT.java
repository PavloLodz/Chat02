package pl.ldz.chat.controller.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pl.ldz.chat.controller.AbstractControllerIT;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.auth.LoginRequestDto;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.UserService;

class AuthIT extends AbstractControllerIT {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldLoginAndAccessProtectedResource() throws Exception {
    // 1. Create a user (using a method that encodes the password correctly now)
    UserRequestDto userRequest = new UserRequestDto(
        "authtest",
        "authtest@example.com",
        "password123",
        "Auth Test",
        null,
        false,
        "ADMIN"
    );
    userService.create(userRequest);

    // 2. Login
    LoginRequestDto loginRequest = new LoginRequestDto("authtest", "password123");
    String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andReturn().getResponse().getContentAsString();

    String token = objectMapper.readTree(loginResponse).get("token").asText();

    // 3. Access protected resource with token
    mockMvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn401ForInvalidCredentials() throws Exception {
    LoginRequestDto loginRequest = new LoginRequestDto("nonexistent", "wrongpass");
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldReturn401ForInvalidToken() throws Exception {
    mockMvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }
}
