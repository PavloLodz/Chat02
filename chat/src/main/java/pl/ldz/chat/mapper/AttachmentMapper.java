package pl.ldz.chat.mapper;

import org.mapstruct.*;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.dto.AttachmentResponseDto;
import pl.ldz.chat.entity.Attachment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

  @Mapping(source = "message.id", target = "messageId")
  AttachmentResponseDto toResponseDto(Attachment entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "message", ignore = true) // Handled in service
  Attachment toEntity(AttachmentRequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "message", ignore = true) // Handled in service
  void updateEntityFromDto(AttachmentRequestDto requestDto, @MappingTarget Attachment entity);

  List<AttachmentResponseDto> toResponseDtoList(List<Attachment> entities);
}
