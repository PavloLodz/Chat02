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

public class UserServiceIntegrationTest extends AbstractServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Test
  void shouldCreateUser() {
    // given
    UserRequestDto request = UserRequestDto.builder()
        .username("serviceuser")
        .email("service@example.com")
        .passwordHash("hash")
        .online(true)
        .build();

    // when
    UserResponseDto response = userService.create(request);

    // then
    assertThat(response.getId()).isNotNull();
    assertThat(response.getUsername()).isEqualTo("serviceuser");
    assertThat(response.getCreationTimestamp()).isNotNull();
    assertThat(response.getUpdateTimestamp()).isNotNull();
  }

  @Test
  void shouldGetById() {
    // given
    UserRequestDto request = UserRequestDto.builder()
        .username("getuser")
        .email("get@example.com")
        .passwordHash("hash")
        .online(false)
        .build();
    UserResponseDto created = userService.create(request);

    // when
    UserResponseDto found = userService.getById(created.getId());

    // then
    assertThat(found.getId()).isEqualTo(created.getId());
    assertThat(found.getUsername()).isEqualTo("getuser");
  }

  @Test
  void shouldThrowWhenNotFound() {
    assertThatThrownBy(() -> userService.getById(UUID.randomUUID()))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldUpdateUser() {
    // given
    UserRequestDto request = UserRequestDto.builder()
        .username("updateuser")
        .email("update@example.com")
        .passwordHash("hash")
        .online(false)
        .build();
    UserResponseDto created = userService.create(request);

    UserRequestDto updateRequest = UserRequestDto.builder()
        .username("updateuser")
        .email("updated@example.com")
        .passwordHash("hash")
        .displayName("New Display Name")
        .online(true)
        .build();

    // when
    UserResponseDto updated = userService.update(created.getId(), updateRequest);

    // then
    assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    assertThat(updated.getDisplayName()).isEqualTo("New Display Name");
    assertThat(updated.isOnline()).isTrue();
  }

  @Test
  void shouldDeleteUser() {
    // given
    UserRequestDto request = UserRequestDto.builder()
        .username("deleteuser")
        .email("delete@example.com")
        .passwordHash("hash")
        .online(false)
        .build();
    UserResponseDto created = userService.create(request);

    // when
    userService.delete(created.getId());

    // then
    assertThatThrownBy(() -> userService.getById(created.getId()))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void shouldGetAllPaginated() {
    // given
    for (int i = 0; i < 10; i++) {
      userService.create(UserRequestDto.builder()
          .username("user" + i)
          .email("user" + i + "@example.com")
          .passwordHash("hash")
          .online(false)
          .build());
    }

    // when
    Page<UserResponseDto> page = userService.getAll(PageRequest.of(0, 5));

    // then
    assertThat(page.getContent()).hasSize(5);
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(10);
  }
}
