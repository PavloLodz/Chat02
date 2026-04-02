package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id")
  private ChatRoom chatRoom;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_chat_id")
  private PersonalChat personalChat;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder.Default
  @Column(name = "edited", nullable = false)
  private boolean edited = false;

  @Builder.Default
  @Column(name = "deleted", nullable = false)
  private boolean deleted = false;

  @Override
  public String toString() {
    return "Message{" +
      "id=" + getId() +
      ", content='" + (content != null && content.length() > 20 ? content.substring(0, 20) + "..." : content) + '\'' +
      '}';
  }
}
