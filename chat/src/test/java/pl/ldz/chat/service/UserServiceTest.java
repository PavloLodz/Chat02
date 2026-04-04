package pl.ldz.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.UserMapper;
import pl.ldz.chat.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(passwordEncoder);
        userService = new UserService(userRepository, userMapper);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UserRequestDto buildRequest(String suffix) {
        return new UserRequestDto(
                "user_" + suffix,
                "user_" + suffix + "@example.com",
                "hash_" + suffix,
                "Display " + suffix,
                null,
                false,
                "USER"
        );
    }

    private User buildUser(UUID id, String suffix) {
        User user = new User();
        user.setUsername("user_" + suffix);
        user.setEmail("user_" + suffix + "@example.com");
        user.setPasswordHash("hash_" + suffix);
        user.setDisplayName("Display " + suffix);
        user.setRole("USER");
        return user;
    }

    private UserResponseDto buildResponseDto(UUID id, String suffix) {
        return new UserResponseDto(
                id,
                0L,
                Instant.now(),
                Instant.now(),
                "user_" + suffix,
                "user_" + suffix + "@example.com",
                "Display " + suffix,
                null,
                false,
                "USER"
        );
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldMapSaveAndReturnResponseDto() {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("1");
        User entity = buildUser(id, "1");

        when(userRepository.save(any(User.class))).thenReturn(entity);

        UserResponseDto result = userService.create(request);

        assertThat(result.username()).isEqualTo(request.username());
        assertThat(result.email()).isEqualTo(request.email());
        verify(userRepository).save(any(User.class));
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnResponseDtoWhenUserExists() {
        UUID id = UUID.randomUUID();
        User entity = buildUser(id, "2");

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        UserResponseDto result = userService.getById(id);

        assertThat(result.username()).isEqualTo(entity.getUsername());
        verify(userRepository).findById(id);
    }

    @Test
    void getById_shouldThrowEntityNotFoundExceptionWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).findById(id);
    }

    // -------------------------------------------------------------------------
    // getAll
    // -------------------------------------------------------------------------

    @Test
    void getAll_shouldReturnPageOfResponseDtos() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        User user1 = buildUser(id1, "3");
        User user2 = buildUser(id2, "4");

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = userService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).username()).isEqualTo(user1.getUsername());
        assertThat(result.getContent().get(1).username()).isEqualTo(user2.getUsername());
    }

    @Test
    void getAll_shouldReturnEmptyPageWhenNoUsersExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        Page<UserResponseDto> result = userService.getAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldApplyChangesAndReturnUpdatedResponseDto() {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("5");
        User entity = buildUser(id, "5");

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        UserResponseDto result = userService.update(id, request);

        assertThat(result.username()).isEqualTo(request.username());
        verify(userRepository).save(entity);
    }

    @Test
    void update_shouldThrowEntityNotFoundExceptionWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("6");
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldInvokeDeleteByIdWhenUserExists() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(userRepository).deleteById(id);

        userService.delete(id);

        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(any());
    }
}
