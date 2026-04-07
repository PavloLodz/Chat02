package pl.ldz.chat.repository;

import pl.ldz.chat.entity.Message;
import pl.ldz.chat.repository.base.AbstractRepository;
import java.util.UUID;

public interface MessageRepository extends AbstractRepository<Message, UUID> {
}
