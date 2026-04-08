package pl.ldz.chat.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.ldz.chat.entity.RoomMember;
import pl.ldz.chat.repository.base.AbstractRepository;
import java.util.List;
import java.util.UUID;

public interface RoomMemberRepository extends AbstractRepository<RoomMember, UUID> {

  //TODO: check!
  @Query("SELECT rm FROM RoomMember rm WHERE rm.room.id = :roomId")
  List<RoomMember> findByRoomId(@Param("roomId") UUID roomId);
}
