package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

@Entity
@Table(name = "friend_requests",
  uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  private User receiver;

  // TODO: PENDING, ACCEPTED, DECLINED
  @Builder.Default
  @Column(name = "status", nullable = false, length = 20)
  private String status = "PENDING";

  @Override
  public String toString() {
    return "FriendRequest{" +
      "id=" + getId() +
      ", status='" + status + '\'' +
      '}';
  }
}
