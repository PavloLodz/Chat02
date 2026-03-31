package pl.ldz.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.service.base.AbstractServiceIntegrationTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceIntegrationTest extends AbstractServiceIntegrationTest {

  @Autowired
  private UserService userService;

  private UserRequestDto buildRequest(String suffix) {
    return new UserRequestDto(
      "user_" + suffix,
      "user_" + suffix + "@example.com",
      "hash_" + suffix,
      "Display " + suffix,
      null,
      false
    );
  }

  @Test
  void shouldCreateUser() {
    UserResponseDto response = userService.create(buildRequest("svc1"));

    assertThat(response.id()).isNotNull();
    assertThat(response.username()).isEqualTo("user_svc1");
    assertThat(response.creationTimestamp()).isNotNull();
  }

  @Test
  void shouldGetById() {
    UserResponseDto created = userService.create(buildRequest("svc2"));
    UserResponseDto found = userService.getById(created.id());

    assertThat(found.username()).isEqualTo("user_svc2");
  }

  @Test
  void shouldThrowWhenUserNotFound() {
    UUID randomId = UUID.randomUUID();

    assertThatThrownBy(() -> userService.getById(randomId))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining(randomId.toString());
  }

  @Test
  void shouldReturnPagedUsers() {
    userService.create(buildRequest("svc3"));
    userService.create(buildRequest("svc4"));

    Page<UserResponseDto> page = userService.getAll(PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void shouldUpdateUser() {
    UserResponseDto created = userService.create(buildRequest("svc5"));
    UserRequestDto updateRequest = new UserRequestDto(
      "user_svc5", "user_svc5@example.com", "newhash", "New Name", null, true
    );

    UserResponseDto updated = userService.update(created.id(), updateRequest);

    assertThat(updated.displayName()).isEqualTo("New Name");
    assertThat(updated.online()).isTrue();
  }

  @Test
  void shouldDeleteUser() {
    UserResponseDto created = userService.create(buildRequest("svc6"));

    userService.delete(created.id());

    assertThatThrownBy(() -> userService.getById(created.id()))
      .isInstanceOf(EntityNotFoundException.class);
  }
}
