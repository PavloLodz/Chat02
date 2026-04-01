package pl.ldz.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
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

  @Column(name = "online", nullable = false)
  private boolean online = false;

  @Column(name = "role", nullable = false, length = 20)
  private String role = "USER";

  public User() {}

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

  public boolean isOnline() { return online; }
  public void setOnline(boolean online) { this.online = online; }

  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
}
