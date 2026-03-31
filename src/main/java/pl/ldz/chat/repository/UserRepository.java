package pl.ldz.chat.repository;

import org.springframework.stereotype.Repository;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.UUID;

@Repository
public interface UserRepository extends AbstractRepository<User, UUID> {
}
