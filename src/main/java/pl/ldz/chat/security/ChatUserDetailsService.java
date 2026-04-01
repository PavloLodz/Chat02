package pl.ldz.chat.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.ldz.chat.entity.User;
import pl.ldz.chat.repository.UserRepository;

import java.util.List;

@Service
public class ChatUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public ChatUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
    );
  }
}
