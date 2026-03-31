package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "room_bans",
  uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
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

  public RoomBan() {}

  public ChatRoom getRoom() { return room; }
  public void setRoom(ChatRoom room) { this.room = room; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public User getBannedBy() { return bannedBy; }
  public void setBannedBy(User bannedBy) { this.bannedBy = bannedBy; }

  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}
