package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends AbstractEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  public ChatRoom() {}

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public boolean isPublic() { return isPublic; }
  public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

  public User getOwner() { return owner; }
  public void setOwner(User owner) { this.owner = owner; }
}
