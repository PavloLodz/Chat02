package pl.ldz.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.ChatRoomRepository;
import pl.ldz.chat.repository.UserRepository;

@Component
public class DataSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ChatRoomRepository chatRoomRepository;

  @Value("${app.seed.chat-rooms:true}")
  private boolean seedChatRooms;

  public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                    ChatRoomRepository chatRoomRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.chatRoomRepository = chatRoomRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedUser("viewer",  "viewer@chat.local",  "Viewer User",  "vp",  "VIEWER");
    seedUser("user",    "user@chat.local",    "Regular User", "up",    "USER");
    seedUser("admin",   "admin@chat.local",   "Admin User",   "ap",   "ADMIN");
    seedUser("auditor", "auditor@chat.local", "Auditor User", "ap", "AUDITOR");

    if (seedChatRooms) {
      User admin = userRepository.findByUsername("admin").orElseThrow();
      User user  = userRepository.findByUsername("user").orElseThrow();

      seedChatRoom("General",        "General discussion for everyone",        true,  admin);
      seedChatRoom("Random",         "Off-topic chat and fun stuff",           true,  admin);
      seedChatRoom("Announcements",  "Important updates and news",             true,  admin);
      seedChatRoom("Tech Talk",      "Programming, tools and tech discussions",true,  user);
      seedChatRoom("Private Lounge", "Invite-only members lounge",             false, admin);
    } else {
      log.info("Chat room seeding skipped (app.seed.chat-rooms=false).");
    }

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

  private void seedChatRoom(String name, String description, boolean isPublic, User owner) {
    boolean exists = chatRoomRepository.findAll(Pageable.unpaged())
        .stream().anyMatch(r -> r.getName().equals(name));
    if (exists) {
      log.info("Chat room '{}' already exists — skipping.", name);
      return;
    }
    ChatRoom room = ChatRoom.builder()
        .name(name)
        .description(description)
        .isPublic(isPublic)
        .owner(owner)
        .build();
    chatRoomRepository.save(room);
    log.info("Seeded chat room '{}' (public={}, owner={}).", name, isPublic, owner.getUsername());
  }
}
