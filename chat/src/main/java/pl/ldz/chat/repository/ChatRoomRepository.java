package pl.ldz.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends AbstractRepository<ChatRoom, UUID> {

  @Override
  @EntityGraph(attributePaths = {"owner"})
  Optional<ChatRoom> findById(UUID id);

  @Override
  @EntityGraph(attributePaths = {"owner"})
  Page<ChatRoom> findAll(Pageable pageable);
}
