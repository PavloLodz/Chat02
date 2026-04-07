package pl.ldz.chat.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.dto.PersonalChatRequestDto;
import pl.ldz.chat.dto.PersonalChatResponseDto;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class PersonalChatMapper {

  @Autowired
  protected UserRepository userRepository;

  @Mapping(target = "user1", source = "user1Id", qualifiedByName = "idToUser")
  @Mapping(target = "user2", source = "user2Id", qualifiedByName = "idToUser")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  public abstract PersonalChat toEntity(PersonalChatRequestDto requestDto);

  public abstract PersonalChatResponseDto toResponseDto(PersonalChat entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "user1", source = "user1Id", qualifiedByName = "idToUser")
  @Mapping(target = "user2", source = "user2Id", qualifiedByName = "idToUser")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  public abstract void updateEntityFromDto(PersonalChatRequestDto requestDto, @MappingTarget PersonalChat entity);

  public abstract List<PersonalChatResponseDto> toResponseDtoList(List<PersonalChat> entities);

  @Named("idToUser")
  protected User idToUser(UUID id) {
    if (id == null) {
      return null;
    }
    return userRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("User", id));
  }
}
