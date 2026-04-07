package pl.ldz.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import pl.ldz.chat.entity.PersonalChat;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonalChatRepository extends AbstractRepository<PersonalChat, UUID> {

  @Override
  @EntityGraph(attributePaths = {"user1", "user2"})
  Optional<PersonalChat> findById(UUID id);

  @Override
  @EntityGraph(attributePaths = {"user1", "user2"})
  Page<PersonalChat> findAll(Pageable pageable);
}
