package pl.ldz.chat.entity;
import jakarta.persistence.*;
import lombok.*;
import pl.ldz.chat.entity.base.AbstractEntity;

import java.util.Objects;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_users_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AbstractEntity {

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Builder.Default
  @Column(name = "online", nullable = false)
  private boolean online = false;

  @Builder.Default
  @Column(name = "role", nullable = false, length = 20)
  private String role = "USER";

  @Override
  public String toString() {
    return "User{" +
      "id=" + getId() +
      ", username='" + username + '\'' +
      ", email='" + email + '\'' +
      '}';
  }
}
