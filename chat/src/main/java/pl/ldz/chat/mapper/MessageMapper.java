package pl.ldz.chat.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.dto.MessageRequestDto;
import pl.ldz.chat.dto.MessageResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.PersonalChatRepository;
import pl.ldz.chat.repository.UserRepository;

import java.util.UUID;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class MessageMapper {

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected ChatRoomRepository chatRoomRepository;

  @Autowired
  protected PersonalChatRepository personalChatRepository;

  @Mapping(target = "sender", source = "senderId", qualifiedByName = "idToUser")
  @Mapping(target = "chatRoom", source = "chatRoomId", qualifiedByName = "idToChatRoom")
  @Mapping(target = "personalChat", source = "personalChatId", qualifiedByName = "idToPersonalChat")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "edited", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  public abstract Message toEntity(MessageRequestDto requestDto);

  @Mapping(target = "chatRoomId", source = "chatRoom.id")
  @Mapping(target = "personalChatId", source = "personalChat.id")
  @Mapping(target = "createdAt", source = "creationTimestamp")
  @Mapping(target = "updatedAt", source = "updateTimestamp")
  public abstract MessageResponseDto toResponseDto(Message entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "sender", source = "senderId", qualifiedByName = "idToUser")
  @Mapping(target = "chatRoom", source = "chatRoomId", qualifiedByName = "idToChatRoom")
  @Mapping(target = "personalChat", source = "personalChatId", qualifiedByName = "idToPersonalChat")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "creationTimestamp", ignore = true)
  @Mapping(target = "updateTimestamp", ignore = true)
  @Mapping(target = "edited", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  public abstract void updateEntityFromDto(MessageRequestDto requestDto, @MappingTarget Message entity);

  @Named("idToUser")
  protected User idToUser(UUID id) {
    if (id == null) return null;
    return userRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("User", id));
  }

  @Named("idToChatRoom")
  protected ChatRoom idToChatRoom(UUID id) {
    if (id == null) return null;
    return chatRoomRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("ChatRoom", id));
  }

  @Named("idToPersonalChat")
  protected PersonalChat idToPersonalChat(UUID id) {
    if (id == null) return null;
    return personalChatRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("PersonalChat", id));
  }
}
