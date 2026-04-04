package pl.ldz.chat.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final UserMapper userMapper = new UserMapper(passwordEncoder);

  @Test
  void shouldMapToEntity() {
    UserRequestDto dto = new UserRequestDto(
      "testuser",
      "test@example.com",
      "password",
      "Test User",
      "http://avatar.url",
      true,
      "USER"
    );

    User user = userMapper.toEntity(dto);

    assertEquals(dto.username(), user.getUsername());
    assertEquals(dto.email(), user.getEmail());
    assertTrue(passwordEncoder.matches("password", user.getPasswordHash()));
    assertEquals(dto.displayName(), user.getDisplayName());
    assertEquals(dto.avatarUrl(), user.getAvatarUrl());
    assertTrue(user.isOnline());
    assertEquals("USER", user.getRole());
  }

  @Test
  void shouldMapToResponseDto() {
    User user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setDisplayName("Test User");
    user.setOnline(true);
    user.setRole("USER");

    UserResponseDto dto = userMapper.toResponseDto(user);

    assertEquals(user.getUsername(), dto.username());
    assertEquals(user.getEmail(), dto.email());
    assertEquals(user.getDisplayName(), dto.displayName());
    assertTrue(dto.online());
    assertEquals("USER", dto.role());
  }
}
