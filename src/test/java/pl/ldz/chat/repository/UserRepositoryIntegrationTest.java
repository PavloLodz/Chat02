package pl.ldz.chat.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepositoryIntegrationTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryIntegrationTest extends AbstractRepositoryIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  void shouldSaveAndAuditingUser() {
    // given
    User user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedpassword");
    user.setOnline(true);

    // when
    User savedUser = userRepository.save(user);

    // then
    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getCreationTimestamp()).isNotNull();
    assertThat(savedUser.getUpdateTimestamp()).isNotNull();
    assertThat(savedUser.getVersion()).isNotNull();
    assertThat(savedUser.getUsername()).isEqualTo("testuser");

    // when (update)
    Instant firstUpdate = savedUser.getUpdateTimestamp();
    savedUser.setDisplayName("Updated Name");
    User updatedUser = userRepository.saveAndFlush(savedUser);

    // then
    assertThat(updatedUser.getDisplayName()).isEqualTo("Updated Name");
    assertThat(updatedUser.getCreationTimestamp()).isEqualTo(savedUser.getCreationTimestamp());
    assertThat(updatedUser.getUpdateTimestamp()).isAfterOrEqualTo(firstUpdate);
  }

  @Test
  void shouldFindById() {
    // given
    User user = new User();
    user.setUsername("findme");
    user.setEmail("findme@example.com");
    user.setPasswordHash("pass");
    User saved = userRepository.save(user);

    // when
    Optional<User> found = userRepository.findById(saved.getId());

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("findme");
  }
}
