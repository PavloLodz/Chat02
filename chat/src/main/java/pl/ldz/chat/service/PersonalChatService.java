package pl.ldz.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.dto.PersonalChatRequestDto;
import pl.ldz.chat.dto.PersonalChatResponseDto;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.mapper.PersonalChatMapper;
import pl.ldz.chat.repository.PersonalChatRepository;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalChatService implements CrudService<PersonalChat, UUID, PersonalChatRequestDto, PersonalChatResponseDto> {

  private final PersonalChatRepository personalChatRepository;
  private final PersonalChatMapper personalChatMapper;

  @Override
  @Transactional
  public PersonalChatResponseDto create(PersonalChatRequestDto request) {
    PersonalChat entity = personalChatMapper.toEntity(request);
    PersonalChat saved = personalChatRepository.save(entity);
    return personalChatMapper.toResponseDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public PersonalChatResponseDto getById(UUID id) {
    return personalChatRepository.findById(id)
      .map(personalChatMapper::toResponseDto)
      .orElseThrow(() -> new EntityNotFoundException("PersonalChat", id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PersonalChatResponseDto> getAll(Pageable pageable) {
    return personalChatRepository.findAll(pageable)
      .map(personalChatMapper::toResponseDto);
  }

  @Override
  @Transactional
  public PersonalChatResponseDto update(UUID id, PersonalChatRequestDto request) {
    PersonalChat entity = personalChatRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("PersonalChat", id));
    personalChatMapper.updateEntityFromDto(request, entity);
    PersonalChat updated = personalChatRepository.save(entity);
    return personalChatMapper.toResponseDto(updated);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!personalChatRepository.existsById(id)) {
      throw new EntityNotFoundException("PersonalChat", id);
    }
    personalChatRepository.deleteById(id);
  }
}
