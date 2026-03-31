package pl.ldz.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "room_invitations")
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

  @Column(name = "status", nullable = false, length = 20)
  private String status = "PENDING";
}
