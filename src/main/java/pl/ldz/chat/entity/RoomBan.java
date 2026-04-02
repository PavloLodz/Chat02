package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

@Entity
@Table(name = "room_bans",
  uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBan extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "banned_by_id", nullable = false)
  private User bannedBy;

  @Column(name = "reason")
  private String reason;

  @Override
  public String toString() {
    return "RoomBan{" +
      "id=" + getId() +
      '}';
  }
}
