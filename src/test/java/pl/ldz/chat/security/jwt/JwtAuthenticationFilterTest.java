package pl.ldz.chat.security.jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

  private JwtService jwtService;
  private UserDetailsService userDetailsService;
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", "secret-key-must-be-at-least-32-characters-long-and-secure");
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    
    userDetailsService = mock(UserDetailsService.class);
    jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);

    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldNotFilterWhenNoAuthHeader() throws ServletException, IOException {
    // given
    when(request.getHeader("Authorization")).thenReturn(null);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldAuthenticateWhenValidToken() throws ServletException, IOException {
    // given
    String username = "testuser";
    UserDetails userDetails = User.builder()
        .username(username)
        .password("password")
        .authorities("ROLE_USER")
        .build();
    
    String jwt = jwtService.generateToken(userDetails);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain).doFilter(request, response);
    assert SecurityContextHolder.getContext().getAuthentication() != null;
    assert SecurityContextHolder.getContext().getAuthentication().getName().equals(username);
  }
}
