package pl.ldz.chat.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", "secret-key-must-be-at-least-32-characters-long-and-secure");
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
  }

  @Test
  void shouldGenerateToken() {
    // given
    String username = "testuser";
    UserDetails userDetails = User.builder()
        .username(username)
        .password("password")
        .authorities("ROLE_USER")
        .build();

    // when
    String token = jwtService.generateToken(userDetails);

    // then
    assertNotNull(token);
    assertEquals(username, jwtService.getUsernameFromToken(token));
    List<String> roles = jwtService.getRolesFromToken(token);
    assertTrue(roles.contains("ROLE_USER"));
  }

  @Test
  void shouldValidateToken() {
    // given
    String username = "testuser";
    UserDetails userDetails = User.builder()
        .username(username)
        .password("password")
        .authorities("ROLE_USER")
        .build();
    String token = jwtService.generateToken(userDetails);

    // when
    boolean isValid = jwtService.validateToken(token, userDetails);

    // then
    assertTrue(isValid);
  }
}
