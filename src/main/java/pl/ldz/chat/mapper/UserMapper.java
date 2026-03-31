package pl.ldz.chat.mapper;

import org.mapstruct.*;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponseDto toResponseDto(User entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  User toEntity(UserRequestDto requestDto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  void updateEntityFromDto(UserRequestDto requestDto, @MappingTarget User entity);

  List<UserResponseDto> toResponseDtoList(List<User> entities);
}
