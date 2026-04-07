package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.ChatRoomRequestDto;
import pl.ldz.chat.dto.ChatRoomResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.ChatRoomMapper;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.UserRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomService implements CrudService<ChatRoom, UUID, ChatRoomRequestDto, ChatRoomResponseDto> {

  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ChatRoomMapper chatRoomMapper;

  @Override
  public ChatRoomResponseDto create(ChatRoomRequestDto request) {
    User owner = userRepository.findById(request.ownerId())
        .orElseThrow(() -> new EntityNotFoundException("User", request.ownerId()));
    
    ChatRoom chatRoom = chatRoomMapper.toEntity(request);
    chatRoom.setOwner(owner);
    
    ChatRoom saved = chatRoomRepository.save(chatRoom);
    return chatRoomMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public ChatRoomResponseDto getById(UUID id) {
    ChatRoom chatRoom = chatRoomRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom", id));
    return chatRoomMapper.toResponseDto(chatRoom);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChatRoomResponseDto> getAll(Pageable pageable) {
    return chatRoomRepository.findAll(pageable)
        .map(chatRoomMapper::toResponseDto);
  }

  @Override
  public ChatRoomResponseDto update(UUID id, ChatRoomRequestDto request) {
    ChatRoom chatRoom = chatRoomRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("ChatRoom", id));
    
    if (!chatRoom.getOwner().getId().equals(request.ownerId())) {
      User newOwner = userRepository.findById(request.ownerId())
          .orElseThrow(() -> new EntityNotFoundException("User", request.ownerId()));
      chatRoom.setOwner(newOwner);
    }
    
    chatRoomMapper.updateEntityFromDto(request, chatRoom);
    ChatRoom saved = chatRoomRepository.save(chatRoom);
    return chatRoomMapper.toResponseDto(saved);
  }

  @Override
  public void delete(UUID id) {
    if (!chatRoomRepository.existsById(id)) {
      throw new EntityNotFoundException("ChatRoom", id);
    }
    chatRoomRepository.deleteById(id);
  }
}
