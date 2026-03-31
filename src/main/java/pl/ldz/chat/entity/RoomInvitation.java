package pl.ldz.chat.entity;

import jakarta.persistence.*;

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

  public RoomInvitation() {}

  public ChatRoom getRoom() { return room; }
  public void setRoom(ChatRoom room) { this.room = room; }

  public User getInvitedBy() { return invitedBy; }
  public void setInvitedBy(User invitedBy) { this.invitedBy = invitedBy; }

  public User getInvitedUser() { return invitedUser; }
  public void setInvitedUser(User invitedUser) { this.invitedUser = invitedUser; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
