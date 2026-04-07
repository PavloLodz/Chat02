package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.MessageRequestDto;
import pl.ldz.chat.dto.MessageResponseDto;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.MessageMapper;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService implements CrudService<Message, UUID, MessageRequestDto, MessageResponseDto> {

  private final MessageRepository messageRepository;
  private final MessageMapper messageMapper;

  @Override
  @Transactional
  public MessageResponseDto create(MessageRequestDto request) {
    validateChatReference(request);
    Message entity = messageMapper.toEntity(request);
    Message saved = messageRepository.save(entity);
    return messageMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public MessageResponseDto getById(UUID id) {
    return messageRepository.findById(id)
      .map(messageMapper::toResponseDto)
      .orElseThrow(() -> new EntityNotFoundException("Message", id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<MessageResponseDto> getAll(Pageable pageable) {
    return messageRepository.findAll(pageable)
      .map(messageMapper::toResponseDto);
  }

  @Override
  @Transactional
  public MessageResponseDto update(UUID id, MessageRequestDto request) {
    validateChatReference(request);
    Message entity = messageRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Message", id));
    messageMapper.updateEntityFromDto(request, entity);
    entity.setEdited(true);
    Message updated = messageRepository.save(entity);
    return messageMapper.toResponseDto(updated);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!messageRepository.existsById(id)) {
      throw new EntityNotFoundException("Message", id);
    }
    messageRepository.deleteById(id);
  }

  private void validateChatReference(MessageRequestDto request) {
    if (request.getChatRoomId() == null && request.getPersonalChatId() == null) {
      throw new IllegalArgumentException("Message must belong to either a ChatRoom or a PersonalChat");
    }
    if (request.getChatRoomId() != null && request.getPersonalChatId() != null) {
      throw new IllegalArgumentException("Message cannot belong to both a ChatRoom and a PersonalChat");
    }
  }
}
