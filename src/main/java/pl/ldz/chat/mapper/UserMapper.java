package pl.ldz.chat.mapper;

import org.springframework.stereotype.Component;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

  public UserResponseDto toResponseDto(User user) {
    return new UserResponseDto(
      user.getId(),
      user.getVersion(),
      user.getCreationTimestamp(),
      user.getUpdateTimestamp(),
      user.getUsername(),
      user.getEmail(),
      user.getDisplayName(),
      user.getAvatarUrl(),
      user.isOnline()
    );
  }

  public User toEntity(UserRequestDto dto) {
    User user = new User();
    user.setUsername(dto.username());
    user.setEmail(dto.email());
    user.setPasswordHash(dto.passwordHash());
    user.setDisplayName(dto.displayName());
    user.setAvatarUrl(dto.avatarUrl());
    user.setOnline(dto.online());
    return user;
  }

  public void updateEntityFromDto(UserRequestDto dto, User user) {
    if (dto.username() != null) user.setUsername(dto.username());
    if (dto.email() != null) user.setEmail(dto.email());
    if (dto.passwordHash() != null) user.setPasswordHash(dto.passwordHash());
    if (dto.displayName() != null) user.setDisplayName(dto.displayName());
    if (dto.avatarUrl() != null) user.setAvatarUrl(dto.avatarUrl());
    user.setOnline(dto.online());
  }

  public List<UserResponseDto> toResponseDtoList(List<User> users) {
    return users.stream()
      .map(this::toResponseDto)
      .collect(Collectors.toList());
  }
}
