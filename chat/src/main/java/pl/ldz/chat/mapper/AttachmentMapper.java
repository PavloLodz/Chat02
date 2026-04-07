package pl.ldz.chat.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.dto.AttachmentResponseDto;
import pl.ldz.chat.entity.Attachment;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.MessageRepository;

import java.util.UUID;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class AttachmentMapper {

  @Autowired
  protected MessageRepository messageRepository;

  @Mapping(target = "message", source = "messageId", qualifiedByName = "idToMessage")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  public abstract Attachment toEntity(AttachmentRequestDto requestDto);

  @Mapping(target = "messageId", source = "message.id")
  @Mapping(target = "createdAt", source = "creationTimestamp")
  @Mapping(target = "updatedAt", source = "updateTimestamp")
  public abstract AttachmentResponseDto toResponseDto(Attachment entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "message", source = "messageId", qualifiedByName = "idToMessage")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  public abstract void updateEntityFromDto(AttachmentRequestDto requestDto, @MappingTarget Attachment entity);

  @Named("idToMessage")
  protected Message idToMessage(UUID id) {
    if (id == null) return null;
    return messageRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Message", id));
  }
}
