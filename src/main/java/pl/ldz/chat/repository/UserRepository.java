package pl.ldz.chat.repository;

import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.UUID;

public interface UserRepository extends AbstractRepository<User, UUID> {
}
