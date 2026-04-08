package pl.ldz.chat.repository;

import org.springframework.stereotype.Repository;
import pl.ldz.chat.entity.FriendRequest;
import pl.ldz.chat.repository.base.AbstractRepository;

import java.util.UUID;

@Repository
public interface FriendRequestRepository extends AbstractRepository<FriendRequest, UUID> {
}
