package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom extends AbstractEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description")
  private String description;

  @Builder.Default
  @Column(name = "is_public", nullable = false)
  private boolean isPublic = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Override
  public String toString() {
    return "ChatRoom{" +
      "id=" + getId() +
      ", name='" + name + '\'' +
      '}';
  }
}
