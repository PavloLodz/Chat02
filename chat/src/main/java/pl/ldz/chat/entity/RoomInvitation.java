package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

@Entity
@Table(name = "room_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInvitation extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_by_id", nullable = false)
  private User invitedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invited_user_id", nullable = false)
  private User invitedUser;

  @Builder.Default
  @Column(name = "status", nullable = false, length = 20)
  private String status = "PENDING";

  @Override
  public String toString() {
    return "RoomInvitation{" +
      "id=" + getId() +
      ", status='" + status + '\'' +
      '}';
  }
}
