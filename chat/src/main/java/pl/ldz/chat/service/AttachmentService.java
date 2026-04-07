package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.dto.AttachmentResponseDto;
import pl.ldz.chat.entity.Attachment;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.AttachmentMapper;
import pl.ldz.chat.repository.AttachmentRepository;
import pl.ldz.chat.repository.MessageRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AttachmentService implements CrudService<Attachment, UUID, AttachmentRequestDto, AttachmentResponseDto> {

  private final AttachmentRepository attachmentRepository;
  private final MessageRepository messageRepository;
  private final AttachmentMapper attachmentMapper;

  @Override
  public AttachmentResponseDto create(AttachmentRequestDto request) {
    Message message = messageRepository.findById(request.getMessageId())
        .orElseThrow(() -> new EntityNotFoundException("Message", request.getMessageId()));

    Attachment attachment = attachmentMapper.toEntity(request);
    attachment.setMessage(message);
    Attachment saved = attachmentRepository.save(attachment);
    return attachmentMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public AttachmentResponseDto getById(UUID id) {
    Attachment attachment = attachmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Attachment", id));
    return attachmentMapper.toResponseDto(attachment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AttachmentResponseDto> getAll(Pageable pageable) {
    return attachmentRepository.findAll(pageable)
        .map(attachmentMapper::toResponseDto);
  }

  @Override
  public AttachmentResponseDto update(UUID id, AttachmentRequestDto request) {
    Attachment attachment = attachmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Attachment", id));

    if (!attachment.getMessage().getId().equals(request.getMessageId())) {
      Message newMessage = messageRepository.findById(request.getMessageId())
          .orElseThrow(() -> new EntityNotFoundException("Message", request.getMessageId()));
      attachment.setMessage(newMessage);
    }

    attachmentMapper.updateEntityFromDto(request, attachment);
    Attachment saved = attachmentRepository.save(attachment);
    return attachmentMapper.toResponseDto(saved);
  }

  @Override
  public void delete(UUID id) {
    if (!attachmentRepository.existsById(id)) {
      throw new EntityNotFoundException("Attachment", id);
    }
    attachmentRepository.deleteById(id);
  }
}
