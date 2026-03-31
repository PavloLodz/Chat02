package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "friend_requests",
  uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
public class FriendRequest extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  @Column(name = "status", nullable = false, length = 20)
  private String status = "PENDING";

  public FriendRequest() {}

  public User getSender() { return sender; }
  public void setSender(User sender) { this.sender = sender; }

  public User getReceiver() { return receiver; }
  public void setReceiver(User receiver) { this.receiver = receiver; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
