package pl.ldz.chat.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.ldz.chat.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final ChatUserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthFilter;

  public SecurityConfig(ChatUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
    this.userDetailsService = userDetailsService;
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage())
            )
        )
        .authenticationProvider(authenticationProvider())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("VIEWER", "USER", "ADMIN", "AUDITOR")
            .requestMatchers(HttpMethod.POST, "/api/v1/chat-rooms").hasAnyRole("USER", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/chat-rooms/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/chat-rooms/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/chat-rooms/**").hasAnyRole("VIEWER", "USER", "ADMIN", "AUDITOR")
            .requestMatchers("/api/v1/personal-chats/**").authenticated()
            .requestMatchers("/api/v1/messages/**").authenticated()
            .requestMatchers("/api/v1/attachments/**").authenticated()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
