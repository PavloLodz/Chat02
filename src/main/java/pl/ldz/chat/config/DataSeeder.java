package pl.ldz.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.UserRepository;

@Component
public class DataSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedUser("viewer",  "viewer@chat.local",  "Viewer User",  "vp",  "VIEWER");
    seedUser("user",    "user@chat.local",    "Regular User", "up",    "USER");
    seedUser("admin",   "admin@chat.local",   "Admin User",   "ap",   "ADMIN");
    seedUser("auditor", "auditor@chat.local", "Auditor User", "ap", "AUDITOR");
    log.info("Data seeding complete.");
  }

  private void seedUser(String username, String email, String displayName,
                        String rawPassword, String role) {
    if (userRepository.existsByUsername(username)) {
      log.info("User '{}' already exists — skipping.", username);
      return;
    }
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setDisplayName(displayName);
    user.setPasswordHash(passwordEncoder.encode(rawPassword));
    user.setRole(role);
    user.setOnline(false);
    userRepository.save(user);
    log.info("Seeded user '{}' with role '{}'.", username, role);
  }
}
