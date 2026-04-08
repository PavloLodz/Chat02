package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.dto.RoomMemberResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.RoomMember;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.RoomMemberMapper;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.RoomMemberRepository;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomMemberService implements CrudService<RoomMember, UUID, RoomMemberRequestDto, RoomMemberResponseDto> {

  private final RoomMemberRepository roomMemberRepository;
  private final RoomMemberMapper roomMemberMapper;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;

  @Override
  public RoomMemberResponseDto create(RoomMemberRequestDto request) {
    RoomMember roomMember = roomMemberMapper.toEntity(request);
    roomMember.setRoom(fetchChatRoom(request.roomId()));
    roomMember.setUser(fetchUser(request.userId()));
    RoomMember saved = roomMemberRepository.save(roomMember);
    return roomMemberMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public RoomMemberResponseDto getById(UUID id) {
    RoomMember roomMember = roomMemberRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("RoomMember", id));
    return roomMemberMapper.toResponseDto(roomMember);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RoomMemberResponseDto> getAll(Pageable pageable) {
    return roomMemberRepository.findAll(pageable)
        .map(roomMemberMapper::toResponseDto);
  }

  @Override
  public RoomMemberResponseDto update(UUID id, RoomMemberRequestDto request) {
    RoomMember roomMember = roomMemberRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("RoomMember", id));
    roomMemberMapper.updateEntityFromDto(request, roomMember);
    roomMember.setRoom(fetchChatRoom(request.roomId()));
    roomMember.setUser(fetchUser(request.userId()));
    RoomMember saved = roomMemberRepository.save(roomMember);
    return roomMemberMapper.toResponseDto(saved);
  }

  @Override
  public void delete(UUID id) {
    if (!roomMemberRepository.existsById(id)) {
      throw new EntityNotFoundException("RoomMember", id);
    }
    roomMemberRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<RoomMemberResponseDto> findByRoomId(UUID roomId) {
    return roomMemberMapper.toResponseDtoList(roomMemberRepository.findByRoomId(roomId));
  }

  private ChatRoom fetchChatRoom(UUID id) {
    return chatRoomRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom", id));
  }

  private User fetchUser(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("User", id));
  }
}
