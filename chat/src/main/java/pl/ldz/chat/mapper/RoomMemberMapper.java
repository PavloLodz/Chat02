package pl.ldz.chat.mapper;

import org.mapstruct.*;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.dto.RoomMemberResponseDto;
import pl.ldz.chat.entity.RoomMember;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface RoomMemberMapper {

  @Mapping(target = "roomId", source = "room.id")
  @Mapping(target = "userId", source = "user.id")
  RoomMemberResponseDto toResponseDto(RoomMember entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "room", ignore = true)
  @Mapping(target = "user", ignore = true)
  RoomMember toEntity(RoomMemberRequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "room", ignore = true)
  @Mapping(target = "user", ignore = true)
  void updateEntityFromDto(RoomMemberRequestDto requestDto, @MappingTarget RoomMember entity);

  List<RoomMemberResponseDto> toResponseDtoList(List<RoomMember> entities);
}
