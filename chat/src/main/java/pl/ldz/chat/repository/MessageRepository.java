package pl.ldz.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import pl.ldz.chat.entity.Message;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends AbstractRepository<Message, UUID> {

  @Override
  @EntityGraph(attributePaths = {"sender", "chatRoom", "personalChat"})
  Optional<Message> findById(UUID id);

  @Override
  @EntityGraph(attributePaths = {"sender", "chatRoom", "personalChat"})
  Page<Message> findAll(Pageable pageable);
}
