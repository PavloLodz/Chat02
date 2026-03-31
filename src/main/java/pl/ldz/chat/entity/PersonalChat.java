package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "personal_chats",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"}))
public class PersonalChat extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user1_id", nullable = false)
  private User user1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id", nullable = false)
  private User user2;

  public PersonalChat() {}

  public User getUser1() { return user1; }
  public void setUser1(User user1) { this.user1 = user1; }

  public User getUser2() { return user2; }
  public void setUser2(User user2) { this.user2 = user2; }
}
