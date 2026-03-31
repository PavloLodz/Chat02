package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "unread_messages",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "message_id"}))
public class UnreadMessage extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  public UnreadMessage() {}

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public Message getMessage() { return message; }
  public void setMessage(Message message) { this.message = message; }
}
