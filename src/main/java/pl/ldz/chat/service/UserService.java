package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.UserMapper;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements CrudService<User, UUID, UserRequestDto, UserResponseDto> {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserResponseDto create(UserRequestDto request) {
    User user = userMapper.toEntity(request);
    User saved = userRepository.save(user);
    return userMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponseDto getById(UUID id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("User", id));
    return userMapper.toResponseDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserResponseDto> getAll(Pageable pageable) {
    return userRepository.findAll(pageable)
      .map(userMapper::toResponseDto);
  }

  @Override
  public UserResponseDto update(UUID id, UserRequestDto request) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("User", id));
    userMapper.updateEntityFromDto(request, user);
    User saved = userRepository.save(user);
    return userMapper.toResponseDto(saved);
  }

  @Override
  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException("User", id);
    }
    userRepository.deleteById(id);
  }
}
