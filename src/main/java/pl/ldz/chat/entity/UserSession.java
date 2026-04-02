package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession extends AbstractEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Builder.Default
  @Column(name = "token", nullable = false, unique = true)
  private String token = UUID.randomUUID().toString();

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Override
  public String toString() {
    return "UserSession{" +
      "id=" + getId() +
      ", token='" + token + '\'' +
      '}';
  }
}
