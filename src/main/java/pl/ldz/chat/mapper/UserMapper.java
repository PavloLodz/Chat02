package pl.ldz.chat.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserResponseDto toResponseDto(User user);

  User toEntity(UserRequestDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(UserRequestDto dto, @MappingTarget User user);

  List<UserResponseDto> toResponseDtoList(List<User> users);
}
