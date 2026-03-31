package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "room_members",
  uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
public class RoomMember extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private ChatRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "role", nullable = false, length = 20)
  private String role = "MEMBER";

  public RoomMember() {}

  public ChatRoom getRoom() { return room; }
  public void setRoom(ChatRoom room) { this.room = room; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
}
