package pl.ldz.chat.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.ldz.chat.dto.auth.JwtResponseDto;
import pl.ldz.chat.dto.auth.LoginRequestDto;
import pl.ldz.chat.security.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public JwtResponseDto login(LoginRequestDto loginRequestDto) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.getUsername(),
            loginRequestDto.getPassword()
        )
    );

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    List<String> roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
    String token = jwtService.generateToken(userDetails, roles);
    LocalDateTime expiresAt = LocalDateTime.now()
        .plusNanos(jwtService.getJwtExpiration() * 1_000_000);

    return JwtResponseDto.builder()
        .token(token)
        .expiresAt(expiresAt)
        .build();
  }
}
