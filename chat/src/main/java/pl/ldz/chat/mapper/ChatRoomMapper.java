package pl.ldz.chat.mapper;

import org.mapstruct.*;
import pl.ldz.chat.dto.ChatRoomRequestDto;
import pl.ldz.chat.dto.ChatRoomResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class}, builder = @Builder(disableBuilder = true))
public interface ChatRoomMapper {

  ChatRoomResponseDto toResponseDto(ChatRoom entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "owner", ignore = true) // Set manually in service
  ChatRoom toEntity(ChatRoomRequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "owner", ignore = true) // Set manually if ownerId changes
  void updateEntityFromDto(ChatRoomRequestDto requestDto, @MappingTarget ChatRoom entity);

  List<ChatRoomResponseDto> toResponseDtoList(List<ChatRoom> entities);
}
