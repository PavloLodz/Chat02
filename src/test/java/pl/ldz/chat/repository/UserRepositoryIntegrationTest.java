package pl.ldz.chat.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.base.AbstractRepositoryIntegrationTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIntegrationTest extends AbstractRepositoryIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  private User buildUser(String suffix) {
    User user = new User();
    user.setUsername("user_" + suffix);
    user.setEmail("user_" + suffix + "@example.com");
    user.setPasswordHash("hash_" + suffix);
    return user;
  }

  @Test
  void shouldSaveAndRetrieveUser() {
    User saved = userRepository.save(buildUser("a"));

    Optional<User> found = userRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("user_a");
  }

  @Test
  void shouldSetAuditTimestampsOnCreate() {
    User saved = userRepository.save(buildUser("b"));

    assertThat(saved.getCreationTimestamp()).isNotNull();
    assertThat(saved.getUpdateTimestamp()).isNotNull();
  }

  @Test
  void shouldDeleteUser() {
    User saved = userRepository.save(buildUser("c"));

    userRepository.deleteById(saved.getId());

    assertThat(userRepository.findById(saved.getId())).isEmpty();
  }

  @Test
  void shouldUpdateUser() {
    User saved = userRepository.save(buildUser("d"));
    saved.setDisplayName("Updated Name");
    User updated = userRepository.save(saved);

    assertThat(updated.getDisplayName()).isEqualTo("Updated Name");
    assertThat(updated.getVersion()).isGreaterThan(0);
  }
}
